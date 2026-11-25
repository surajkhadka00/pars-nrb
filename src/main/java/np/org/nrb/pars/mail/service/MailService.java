package np.org.nrb.pars.mail.service;

import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.system.model.SysConfig;
import np.org.nrb.pars.system.repository.SysConfigRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

@Service
public class MailService {

    private static final Logger log = LoggerFactory.getLogger(MailService.class);

    private final SysConfigRepository sysConfigRepository;
    private final Executor mailExecutor;

    public MailService(SysConfigRepository sysConfigRepository, @Qualifier("mailExecutor") Executor mailExecutor) {
        this.sysConfigRepository = sysConfigRepository;
        this.mailExecutor = mailExecutor;
    }

    public void requireConfigured() {
        settings();
    }

    public CompletableFuture<Void> send(String recipient, String subject, String body) {
        MailSettings settings = settings();
        return CompletableFuture.runAsync(() -> deliver(recipient, subject, body, settings), mailExecutor);
    }

    private void deliver(String recipient, String subject, String body, MailSettings settings) {
        try {
            JavaMailSenderImpl sender = new JavaMailSenderImpl();
            sender.setHost(settings.host());
            sender.setPort(settings.port());
            sender.setUsername(settings.username());
            sender.setPassword(settings.password());
            sender.setProtocol(settings.protocol());
            Properties properties = sender.getJavaMailProperties();
            properties.put("mail.transport.protocol", settings.protocol());
            properties.put("mail.smtp.auth", Boolean.toString(settings.auth()));
            properties.put("mail.smtp.starttls.enable", Boolean.toString(settings.startTls()));

            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(new InternetAddress(settings.from(), settings.fromName()));
            helper.setTo(recipient);
            helper.setSubject(subject);
            helper.setText(body, true);
            sender.send(message);
        } catch (Exception ex) {
            log.error("Failed to send mail to {}", recipient, ex);
            throw new IllegalStateException("Could not send mail", ex);
        }
    }

    private MailSettings settings() {
        if (!flag("mail.enabled")) {
            throw Exceptions.badRequest("Mail sending is disabled");
        }
        return new MailSettings(
                required("mail.host"),
                port(),
                required("mail.protocol"),
                required("mail.username"),
                required("mail.password"),
                required("mail.from"),
                optional("mail.from-name", "PARS NRB"),
                flag("mail.smtp.auth"),
                flag("mail.smtp.starttls")
        );
    }

    private int port() {
        try {
            return Integer.parseInt(required("mail.port"));
        } catch (NumberFormatException ex) {
            throw Exceptions.badRequest("Mail setting mail.port is not a number");
        }
    }

    private boolean flag(String name) {
        return "true".equalsIgnoreCase(optional(name, "false"));
    }

    private String required(String name) {
        String value = optional(name, "");
        if (value.isBlank()) {
            throw Exceptions.badRequest("Mail setting " + name + " is not configured");
        }
        return value.trim();
    }

    private String optional(String name, String fallback) {
        return sysConfigRepository.findById(name)
                .map(SysConfig::getValue)
                .filter(value -> value != null && !value.isBlank())
                .orElse(fallback)
                .trim();
    }

    private record MailSettings(
            String host,
            int port,
            String protocol,
            String username,
            String password,
            String from,
            String fromName,
            boolean auth,
            boolean startTls
    ) {
    }
}
