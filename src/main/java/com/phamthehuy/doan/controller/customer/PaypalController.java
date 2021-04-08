package com.phamthehuy.doan.controller.customer;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.phamthehuy.doan.model.request.PaymentRequest;
import com.phamthehuy.doan.repository.CustomerRepository;
import com.phamthehuy.doan.repository.TransactionRepository;
import com.phamthehuy.doan.model.enums.PaypalPaymentIntent;
import com.phamthehuy.doan.model.enums.PaypalPaymentMethod;
import com.phamthehuy.doan.exception.BadRequestException;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.entity.Customer;
import com.phamthehuy.doan.entity.Transaction;
import com.phamthehuy.doan.service.impl.PaypalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Date;


@RestController
@RequestMapping("/pay")
public class PaypalController {
    @Value("${paypal.rate}")
    private Integer rate;

    private String email;

    public static final String URL_PAYPAL_SUCCESS = "/success";
    public static final String URL_PAYPAL_CANCEL = "/cancel";
    private double value;
    private String description;
    private Customer customer;

    private final Logger logger = LoggerFactory.getLogger(getClass());

    final
    Helper helper;

    final
    CustomerRepository customerRepository;

    final
    TransactionRepository transactionRepository;

    final
    PaypalService paypalService;

    public PaypalController(Helper helper, CustomerRepository customerRepository, TransactionRepository transactionRepository, PaypalService paypalService) {
        this.helper = helper;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
        this.paypalService = paypalService;
    }

    @PostMapping("/pay")
    public ResponseEntity<String> pay(@Valid @RequestBody PaymentRequest paymentRequest,
                                      HttpServletRequest request)
            throws BadRequestException {
        if (paymentRequest.getPrice() < 1.0f) {
            return new ResponseEntity<>("Số tiền nạp vào ít nhất 1 đô", HttpStatus.NOT_ACCEPTABLE);
        }
        String cancelUrl = helper.getBaseURL(request) + URL_PAYPAL_CANCEL;
        String successUrl = helper.getBaseURL(request) + URL_PAYPAL_SUCCESS;
        try {
//            value = Math.round(price * 100.0) / 100.0;

            this.value = paymentRequest.getPrice();

            if (paymentRequest.getDescription() == null || paymentRequest.getDescription().trim().equals(""))
                this.description = "Nạp " + this.value + " USD";
            else
                this.description = paymentRequest.getDescription();

            if (email == null || email.trim().equals(""))
                throw new BadRequestException("Không xác định được người dùng");

            customer = customerRepository.findByEmail(email);
            if (customer == null)
                throw new BadRequestException("Không tìm thấy người dùng");

            Payment payment = paypalService.createPayment(
                    this.value,
                    "USD",
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    this.description,
                    cancelUrl,
                    successUrl);

            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    return new ResponseEntity<>(links.getHref(), HttpStatus.OK);
                }
            }
        } catch (PayPalRESTException e) {
            logger.error(e.getMessage());
            throw new BadRequestException("Thanh toán thất bại");
        }
        throw new BadRequestException("Không xác định");
    }

    @GetMapping(URL_PAYPAL_CANCEL)
    public ResponseEntity<String> cancelPay() {
        return new ResponseEntity<>("Thanh toán bị hủy bỏ", HttpStatus.NOT_IMPLEMENTED);
    }

    @GetMapping(URL_PAYPAL_SUCCESS)
    public ResponseEntity<String> successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId)
            throws BadRequestException {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                Integer increase = (int) this.value * rate;
                customer.setAccountBalance(customer.getAccountBalance() + increase);
                customerRepository.save(customer);
                createTransaction(customer, increase);
                return new ResponseEntity<>("Thanh toán thành công, số tiền: " + this.value + " USD - tức " + increase + " VNĐ", HttpStatus.OK);
            }
        } catch (PayPalRESTException e) {
            logger.error(e.getMessage());
            throw new BadRequestException("Có lỗi xảy ra");
        }
        throw new BadRequestException("Không xác định");
    }

    private void createTransaction(Customer customer, Integer money) {
        Transaction transaction = new Transaction();
        transaction.setAmount(money);
        transaction.setType(true);
        transaction.setDescription(description);
        transaction.setTimeCreated(new Date());
        transaction.setCustomer(customer);
        transactionRepository.save(transaction);
    }

}