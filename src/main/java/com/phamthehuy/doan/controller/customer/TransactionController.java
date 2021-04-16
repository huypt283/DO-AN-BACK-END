package com.phamthehuy.doan.controller.customer;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.entity.Transaction;
import com.phamthehuy.doan.exception.AccessDeniedException;
import com.phamthehuy.doan.exception.ConflictException;
import com.phamthehuy.doan.exception.InternalServerError;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.enums.PaypalPaymentIntent;
import com.phamthehuy.doan.model.enums.PaypalPaymentMethod;
import com.phamthehuy.doan.model.request.PaymentRequest;
import com.phamthehuy.doan.model.request.SuccessPaymentRequest;
import com.phamthehuy.doan.model.response.PaymentSuccessResponse;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.TransactionRepository;
import com.phamthehuy.doan.service.impl.PaypalService;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Date;
import java.util.List;


@RestController
@RequestMapping("/customer")
public class TransactionController {
    @Value("${paypal.rate}")
    private Integer rate;

    @Value("${client.url}")
    private String clientUrl;

    public static final String URL_PAYPAL_SUCCESS = "/nap-tien?status=success";
    public static final String URL_PAYPAL_CANCEL = "/nap-tien?status=cancel";

    @Autowired
    private CustomerRepository customerRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private PaypalService paypalService;
    @Autowired
    private Helper helper;

    @GetMapping("/transactions")
    public ResponseEntity<?> listTransaction(@AuthenticationPrincipal UserDetails currentUser) {
        List<Transaction> transactions = transactionRepository.findByCustomer_Email(currentUser.getUsername());
        return new ResponseEntity<>(transactions, HttpStatus.OK);
    }

    @PostMapping("/payment")
    public ResponseEntity<?> payment(@Valid @RequestBody PaymentRequest paymentRequest,
                                     @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        String cancelUrl = this.clientUrl + URL_PAYPAL_CANCEL;
        String successUrl = this.clientUrl + URL_PAYPAL_SUCCESS;

        try {
            Integer value = paymentRequest.getPrice();

            Customer customer = customerRepository.findByEmail(currentUser.getUsername());
            validateCustomer(customer);

            String token = helper.createPaymentToken(20);

            Payment payment = paypalService.createPayment(
                    value,
                    "USD",
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    "Nạp " + value + " USD",
                    cancelUrl + "&payment-token=" + token,
                    successUrl + "&payment-token=" + token);

            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    Integer increase = (int) value * this.rate;
                    createTransaction(customer, increase, token, String.format("Nạp %d USD", value));

                    PaymentSuccessResponse response = new PaymentSuccessResponse();
                    response.setOpenLink(links.getHref());
                    return new ResponseEntity<>(response, HttpStatus.OK);
                }
            }
        } catch (PayPalRESTException e) {
            throw new InternalServerError("Có lỗi xảy ra, thanh toán thất bại");
        }
        throw new InternalServerError("Không xác định được lỗi, bạn vui lòng thử lại");
    }

    @GetMapping("/payment/cancel")
    public ResponseEntity<String> cancelPay(@RequestParam String token,
                                            @AuthenticationPrincipal UserDetails currentUser) {
        Customer customer = customerRepository.findByEmail(currentUser.getUsername());
        validateCustomer(customer);

        Transaction transaction = transactionRepository.findByToken(token);
        if (transaction == null) {
            throw new ConflictException("Giao dịch không hợp lệ");
        }
        transaction.setStatus("Huỷ bỏ");
        transactionRepository.save(transaction);

        return new ResponseEntity<>("Thanh toán bị hủy bỏ", HttpStatus.NOT_IMPLEMENTED);
    }

    @PostMapping("/payment/success")
    public ResponseEntity<String> successPay(@Valid @RequestBody SuccessPaymentRequest successPaymentRequest,
                                             @AuthenticationPrincipal UserDetails currentUser) throws Exception {
        boolean status = false;
        Customer customer = customerRepository.findByEmail(currentUser.getUsername());
        validateCustomer(customer);

        String token = successPaymentRequest.getToken();
        Transaction transaction = transactionRepository.findByToken(token);
        if (transaction != null && transaction.getStatus().equals("Chưa hoàn thành")) {
            try {
                Payment payment = paypalService.executePayment(successPaymentRequest.getPaymentId(), successPaymentRequest.getPayerId());
                if (payment.getState().equals("approved")) {
                    customer.setAccountBalance(customer.getAccountBalance() + transaction.getAmount());
                    customerRepository.save(customer);
                    status = true;
                }
            } catch (PayPalRESTException e) {
                throw new InternalServerError("Có lỗi xảy ra, thanh toán thất bại");
            } finally {
                updateTransaction(transaction, status ? "Thành công" : "Thất bại");
            }
        } else
            throw new ConflictException("Giao dịch không hợp lệ");

        if (status)
            return new ResponseEntity<>("Thanh toán thành công, số tiền: " + transaction.getAmount() + " VNĐ", HttpStatus.OK);
        throw new InternalServerError("Không xác định được lỗi, bạn vui lòng thử lại");
    }

    private void createTransaction(Customer customer, Integer money, String token, String description) {
        Transaction transaction = new Transaction();
        transaction.setAmount(money);
        transaction.setPayment(true);
        transaction.setStatus("Chưa hoàn thành");
        transaction.setToken(token);
        transaction.setDescription(description);
        transaction.setTimeCreated(new Date());
        transaction.setCustomer(customer);
        transactionRepository.save(transaction);
    }

    private void updateTransaction(Transaction transaction, String status) {
        transaction.setStatus(status);
        transactionRepository.save(transaction);
    }

    private void validateCustomer(Customer customer) {
        if (customer == null) {
            throw new ConflictException("Không tìm thấy tài khoản đăng bài");
        } else if (BooleanUtils.isNotTrue(customer.getEnabled()))
            throw new AccessDeniedException("Tài khoản này chưa được kích hoạt");
        else if (BooleanUtils.isTrue(customer.getDeleted()))
            throw new AccessDeniedException("Tài khoản này đã bị khoá");
    }
}