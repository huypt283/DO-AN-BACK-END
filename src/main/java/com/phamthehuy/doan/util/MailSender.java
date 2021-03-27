package com.phamthehuy.doan.util;

import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public class MailSender {
    final
    JavaMailSender javaMailSender;

    public MailSender(JavaMailSender javaMailSender) {
        this.javaMailSender = javaMailSender;
    }

    public void send(String to, String cc, String bcc,
                     String subject, String body, String attachment){
        try {
            String from="phamthehuy283@gmail.com";

            //create mail
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            //user class helper
            MimeMessageHelper mimeMessageHelper=
                    new MimeMessageHelper(mimeMailMessage, true);
            mimeMessageHelper.setFrom(from, from);
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setReplyTo(from, from);
            mimeMessageHelper.setSubject(subject);
            //mimeMessageHelper.setText(body,true);
            mimeMailMessage.setContent(body,"text/html; charset=utf-8");

            javaMailSender.send(mimeMailMessage);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void send(String to,String cc, String bcc, String subject, String body){
        this.send(to, cc, bcc, subject, body, "");
    }

    public void send(String to, String subject, String body){
        this.send(to, "", "", subject, body, "");
    }
}
