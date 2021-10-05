package org.fhi360.ddd.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;

@Service
@RequiredArgsConstructor
public class EmailSender {
    private final JavaMailSender mailSender;

    public void sendMail(String to, String subject, String body) throws MessagingException, UnsupportedEncodingException {

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        message.setFrom(new InternetAddress("zitnaija@gmail.com", "DDD"));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setContent(body, "text/html");
        mailSender.send(message);

    }

    public String activation(String username, String name1, String code) {
        String header = "Decentralized Drugs Distribution (DDD) App";
        String name = "Dear " + username;
        String please = "Your DDD Activation code is";
        String copy = code + "  and your username is  " + name1 +" ," +
            " Kindly use the username for Login after resetting your password using the activation code. ";
        String body2 = "<html>" + "<head>" + "</title>"
            + "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" + "</title>" + "</head>"
            + "<body style=\"margin: 40px; width: 90%\">"
            + "<h2 style=\"font-size: 20px; color: #004D40; font-family: 'lato', sans-serif\">" + header + "</h2>"
            + "<p style=\"font-size: 18px; color: black; font-family: Arial\">" + name + "</p>"
            + please + "<br>" + copy + "<br>"
            + "<a style=\"font-size: 20px; font-weight: bold; color: #004D40; font-family: 'lato', sans-serif\">"
            + "Decentralized Drugs Distribution Application" + "</a><br>"
            + "<a style=\"font-size: 18px; color:red; font-family: 'lato', sans-serif\">"
            + "For support contact us on" + "</a><br>"
            + "<a style=\"font-size: 16px; color:black; font-family: 'lato', sans-serif\">" + "support@fhi360.org."
            + "</a><br>" + "<a style=\"font-size: 16px; color:black; font-family: 'lato', sans-serif\">"
            + "+2347033575836" + "</a><br>" + "</div>" + "</body>" + "</html>";
        return body2;
    }

//    @PostConstruct
//    public void init() throws MessagingException, UnsupportedEncodingException {
//        sendMail("eadrisoyibo@gmail.com", "Test DDD message", activation("oyisco","123"));
//    }
}
