package com.phamthehuy.doan.util;

import com.phamthehuy.doan.exception.InternalServerError;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.mail.internet.MimeMessage;

@Service
public class MailSender {
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private UploadFile uploadFile;

    public void send(String to,
                     String subject, String body, String note, String... attachments) throws Exception {
        try {
            String from = "daihoccongnghiep283@gmail.com";

            //create mail
            MimeMessage mimeMailMessage = javaMailSender.createMimeMessage();
            //user class helper
            MimeMessageHelper mimeMessageHelper =
                    new MimeMessageHelper(mimeMailMessage, true);
            mimeMessageHelper.setFrom(from, "Website tìm trọ");
            mimeMessageHelper.setTo(to);
            mimeMessageHelper.setReplyTo(from, "Website tìm trọ");
            mimeMessageHelper.setSubject(subject);

            String content = "<table style=\"width: 100%\">\n" +
                    "\t<caption><h1>" + subject + "</h1></caption>\n" +
                    "\t<tbody>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td style=\"width: 100%\">" + body + "</td>\n" +
                    "\t\t</tr>\n" +
                    "\t\t<tr><td style=\"width: 100%; text-align: center;\">--------------</td></tr>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td style=\"width: 100%; text-align: left;\"><i style=\"color: red\">" + note + "</i></td>\n" +
                    "\t\t</tr>\n" +
                    "\t\t<tr>\n" +
                    "\t\t\t<td style=\"width: 100%; text-align: center;\">\n" +
                    "\t\t\t<br/><br/><br/><br/><br/>\n" +
                    "\t\t\t<b>Website tìm nhà trọ</b><br/>\n" +
                    "\t\t\t<p style=\"color: blue\"><b>SĐT:</b> 0979859283</p>\n" +
                    "\t\t\t<p style=\"color: blue\"><b>Email:</b> daihoccongnghiep283@gmail.com</p>\n" +
                    "\t\t\t</td>\n" +
                    "\t\t</tr>\n" +
                    "\t</tbody>\n" +
                    "</table>";

            mimeMailMessage.setContent(content, "text/html; charset=utf-8");
            if (attachments.length > 0) {
                for (String attachment : attachments) {
                    mimeMessageHelper.addAttachment(attachment, uploadFile.load(attachment));
                }
            }
            javaMailSender.send(mimeMailMessage);
        } catch (Exception e) {
            throw new InternalServerError("Gửi mail thất bại");
        }
    }
}
