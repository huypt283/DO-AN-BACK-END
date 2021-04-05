package com.phamthehuy.doan.controller.common;

import com.paypal.api.payments.Links;
import com.paypal.api.payments.Payment;
import com.paypal.base.rest.PayPalRESTException;
import com.phamthehuy.doan.dao.CustomerRepository;
import com.phamthehuy.doan.dao.TransactionRepository;
import com.phamthehuy.doan.enums.PaypalPaymentIntent;
import com.phamthehuy.doan.enums.PaypalPaymentMethod;
import com.phamthehuy.doan.exception.CustomException;
import com.phamthehuy.doan.helper.Helper;
import com.phamthehuy.doan.model.dto.output.Message;
import com.phamthehuy.doan.model.entity.Customer;
import com.phamthehuy.doan.model.entity.Transaction;
import com.phamthehuy.doan.service.impl.PaypalService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;


@RestController
public class PaypalController {
    @Value("${paypal.rate}")
    private Integer rate;

    private String email;

    public static final String URL_PAYPAL_SUCCESS = "pay/success";
    public static final String URL_PAYPAL_CANCEL = "pay/cancel";
    private double value;
    private String description;
    private Customer customer;

    private Logger log = LoggerFactory.getLogger(getClass());

    final
    Helper helper;

    final
    CustomerRepository customerRepository;

    final
    TransactionRepository transactionRepository;

    private final PaypalService paypalService;

    public PaypalController(PaypalService paypalService, Helper helper, CustomerRepository customerRepository, TransactionRepository transactionRepository) {
        this.paypalService = paypalService;
        this.helper = helper;
        this.customerRepository = customerRepository;
        this.transactionRepository = transactionRepository;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/pay")
    public Message pay(HttpServletRequest request, //HttpServletResponse response,
                       @RequestParam("price") double price,
                       @RequestParam("email") String email,
                       @RequestParam(required = false) String description)
            throws CustomException {
        String cancelUrl = helper.getBaseURL(request) + "/" + URL_PAYPAL_CANCEL;
        String successUrl = helper.getBaseURL(request) + "/" + URL_PAYPAL_SUCCESS;
        try {
            value = Math.round(price * 100.0) / 100.0;
            //email = helper.getEmailFromRequest(request);
            this.email = email;

            if (description == null || description.trim().equals("")) this.description = "Nạp " + value + " USD";
            else this.description = description;
            if (email == null || email.trim().equals("")) throw new CustomException("Token không hợp lệ");

            customer = customerRepository.findByEmail(email);
            if (customer == null)
                throw new CustomException("Người dùng không hợp lệ");

            Payment payment = paypalService.createPayment(
                    value,
                    "USD",
                    PaypalPaymentMethod.paypal,
                    PaypalPaymentIntent.sale,
                    this.description,
                    cancelUrl,
                    successUrl);
            for (Links links : payment.getLinks()) {
                if (links.getRel().equals("approval_url")) {
                    //response.sendRedirect(links.getHref());
                    return new Message(links.getHref());
                }
            }
        } catch (PayPalRESTException e) {
            e.printStackTrace();
            log.error(e.getMessage());
            throw new CustomException("Thanh toán thất bại");
        }
        //return "redirect:/";
        throw new CustomException("Không xác định");
    }

    @GetMapping(URL_PAYPAL_CANCEL)
    public Message cancelPay() {
        return new Message("Thanh toán bị hủy bỏ");
    }

    @GetMapping(URL_PAYPAL_SUCCESS)
    public Message successPay(@RequestParam("paymentId") String paymentId, @RequestParam("PayerID") String payerId)
            throws CustomException {
        try {
            Payment payment = paypalService.executePayment(paymentId, payerId);
            if (payment.getState().equals("approved")) {
                int increase = (int) (value * rate);
                customer.setAccountBalance(customer.getAccountBalance() + increase);
                customerRepository.save(customer);
                creatTransaction(customer, increase);
                return new Message("Thanh toán thành công, số tiền: " + value + " USD - tức " + increase + " VNĐ");
            }
        } catch (PayPalRESTException e) {
            log.error(e.getMessage());
            throw new CustomException("Có lỗi xảy ra");
        }
        //return "redirect:/";
        throw new CustomException("Không xác định");
    }

    private void creatTransaction(Customer customer, Integer money) {
        Transaction transaction = new Transaction();
        transaction.setAmount(money);
        transaction.setType(true);
        transaction.setDescription(description);
        transaction.setTimeCreated(new Date());
        transaction.setCustomer(customer);
        transactionRepository.save(transaction);
    }

}