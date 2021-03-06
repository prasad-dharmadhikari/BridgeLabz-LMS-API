package com.bridgelabz.lmsapi.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import java.util.Map;
import java.util.Properties;

/**
 * Class to configure mail properties
 */
@Configuration
public class MailConfig {
    /**
     * @return JavaMailSender object
     */
    @Bean
    public JavaMailSender getJavaMailSender() {
        Map<String, String> map = System.getenv();
        String username = map.get("mail.username");
        String password = map.get("mail.password");
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost("smtp.gmail.com");
        mailSender.setPort(587);
        mailSender.setUsername(username);
        mailSender.setPassword(password);
        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        return mailSender;
    }
}
