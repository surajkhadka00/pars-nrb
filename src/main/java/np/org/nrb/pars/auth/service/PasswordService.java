package np.org.nrb.pars.auth.service;

import np.org.nrb.pars.auth.model.AuthSession;
import np.org.nrb.pars.auth.model.PasswordResetToken;
import np.org.nrb.pars.auth.model.dto.ChangePasswordRequest;
import np.org.nrb.pars.auth.model.dto.ForgotPasswordStatusResponse;
import np.org.nrb.pars.auth.model.dto.ResetPasswordRequest;
import np.org.nrb.pars.auth.repository.AuthSessionRepository;
import np.org.nrb.pars.auth.repository.PasswordResetTokenRepository;
import np.org.nrb.pars.common.context.CurrentUser;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.mail.model.MailTemplateCodes;
import np.org.nrb.pars.mail.service.MailService;
import np.org.nrb.pars.mail.service.MailTemplateRenderer;
import np.org.nrb.pars.mail.service.RenderedMail;
import np.org.nrb.pars.system.model.SysConfig;
import np.org.nrb.pars.system.repository.SysConfigRepository;
import np.org.nrb.pars.usermanagement.model.AppUser;
import np.org.nrb.pars.usermanagement.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

@Service
public class PasswordService {

    private static final String FORGOT_MESSAGE = "If the account exists, a password reset mail has been sent.";

    private final AppUserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final AuthSessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenService refreshTokenService;
    private final MailService mailService;
    private final MailTemplateRenderer mailTemplateRenderer;
    private final SysConfigRepository sysConfigRepository;

    public PasswordService(
            AppUserRepository userRepository,
            PasswordResetTokenRepository resetTokenRepository,
            AuthSessionRepository sessionRepository,
            PasswordEncoder passwordEncoder,
            RefreshTokenService refreshTokenService,
            MailService mailService,
            MailTemplateRenderer mailTemplateRenderer,
            SysConfigRepository sysConfigRepository
    ) {
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.sessionRepository = sessionRepository;
        this.passwordEncoder = passwordEncoder;
        this.refreshTokenService = refreshTokenService;
        this.mailService = mailService;
        this.mailTemplateRenderer = mailTemplateRenderer;
        this.sysConfigRepository = sysConfigRepository;
    }

    @Transactional(readOnly = true)
    public ForgotPasswordStatusResponse forgotPasswordStatus(String token) {
        if (token == null || token.isBlank()) {
            throw Exceptions.badRequest("token is required");
        }
        PasswordResetToken reset = resetTokenRepository.findByTokenHash(refreshTokenService.hash(token)).orElse(null);
        Instant now = Instant.now();
        if (reset == null || reset.isUsed() || reset.isExpired(now)) {
            return new ForgotPasswordStatusResponse(false, null);
        }
        return new ForgotPasswordStatusResponse(true, reset.getExpiresAt());
    }

    @Transactional
    public String requestForgotPassword(String email) {
        AppUser user = userRepository.findByEmailIgnoreCase(email.trim()).orElse(null);
        if (user == null || !user.isEnabled() || user.isExpired()) {
            return FORGOT_MESSAGE;
        }
        Instant now = Instant.now();
        for (PasswordResetToken existing : resetTokenRepository.findByUser_IdAndUsedAtIsNull(user.getId())) {
            existing.setUsedAt(now);
        }
        mailService.requireConfigured();
        int minutes = resetTokenMinutes();
        String token = refreshTokenService.generate();
        PasswordResetToken reset = new PasswordResetToken();
        reset.setUser(user);
        reset.setTokenHash(refreshTokenService.hash(token));
        reset.setExpiresAt(now.plusSeconds(minutes * 60L));
        resetTokenRepository.save(reset);
        RenderedMail mail = mailTemplateRenderer.render(MailTemplateCodes.FORGOT_PASSWORD_REQUEST, Map.of(
                "fullName", user.getFullName(),
                "username", user.getUsername(),
                "resetLink", appBaseUrl() + "/reset-password?token=" + token,
                "expiresMinutes", Integer.toString(minutes)
        ));
        sendAfterCommit(user.getEmail(), mail.subject(), mail.body());
        return FORGOT_MESSAGE;
    }

    @Transactional
    public void resetPassword(ResetPasswordRequest request) {
        Instant now = Instant.now();
        PasswordResetToken reset = resetTokenRepository.findByTokenHash(refreshTokenService.hash(request.token()))
                .orElseThrow(() -> Exceptions.badRequest("Reset link is invalid or has expired"));
        if (reset.isUsed() || reset.isExpired(now)) {
            throw Exceptions.badRequest("Reset link is invalid or has expired");
        }
        AppUser user = reset.getUser();
        if (!user.isEnabled() || user.isExpired()) {
            throw Exceptions.forbidden("User account is disabled");
        }
        applyNewPassword(user, request.newPassword(), now);
        reset.setUsedAt(now);
        revokeSessions(user.getId(), now);
        userRepository.save(user);
        mailService.requireConfigured();
        RenderedMail mail = mailTemplateRenderer.render(MailTemplateCodes.PASSWORD_RESET, Map.of(
                "fullName", user.getFullName(),
                "username", user.getUsername()
        ));
        sendAfterCommit(user.getEmail(), mail.subject(), mail.body());
    }

    @Transactional
    public void changePassword(CurrentUser currentUser, ChangePasswordRequest request) {
        AppUser user = userRepository.findById(currentUser.userId())
                .orElseThrow(() -> Exceptions.notFound("User not found"));
        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw Exceptions.unauthorized("Current password is incorrect");
        }
        Instant now = Instant.now();
        applyNewPassword(user, request.newPassword(), now);
        revokeSessions(user.getId(), now);
        userRepository.save(user);
        mailService.requireConfigured();
        RenderedMail mail = mailTemplateRenderer.render(MailTemplateCodes.PASSWORD_CHANGED, Map.of(
                "fullName", user.getFullName(),
                "username", user.getUsername()
        ));
        sendAfterCommit(user.getEmail(), mail.subject(), mail.body());
    }

    private void applyNewPassword(AppUser user, String newPassword, Instant now) {
        if (passwordEncoder.matches(newPassword, user.getPasswordHash())) {
            throw Exceptions.badRequest("New password must be different from the current password");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setPasswordChangedAt(now);
        user.setFailedCount(0);
        user.setLockedAt(null);
        user.setLockedUntil(null);
    }

    private void sendAfterCommit(String recipient, String subject, String body) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            mailService.send(recipient, subject, body);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                mailService.send(recipient, subject, body);
            }
        });
    }

    private int resetTokenMinutes() {
        String value = config("mail.reset-token-minutes", "30");
        try {
            int minutes = Integer.parseInt(value);
            return minutes < 1 ? 30 : minutes;
        } catch (NumberFormatException ex) {
            return 30;
        }
    }

    private String appBaseUrl() {
        String url = config("mail.app-base-url", "");
        if (url.isBlank()) {
            throw Exceptions.badRequest("Mail setting mail.app-base-url is not configured");
        }
        return url.replaceAll("/+$", "");
    }

    private String config(String name, String fallback) {
        return sysConfigRepository.findById(name)
                .map(SysConfig::getValue)
                .filter(value -> value != null && !value.isBlank())
                .orElse(fallback)
                .trim();
    }

    private void revokeSessions(UUID userId, Instant now) {
        for (AuthSession session : sessionRepository.findByUserIdOrderByIssuedAtDesc(userId)) {
            if (!session.isRevoked()) {
                session.setRevokedAt(now);
            }
        }
    }
}
