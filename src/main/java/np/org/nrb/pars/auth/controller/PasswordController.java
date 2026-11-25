package np.org.nrb.pars.auth.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.auth.model.dto.ChangePasswordRequest;
import np.org.nrb.pars.auth.model.dto.ForgotPasswordRequest;
import np.org.nrb.pars.auth.model.dto.ForgotPasswordStatusResponse;
import np.org.nrb.pars.auth.model.dto.ResetPasswordRequest;
import np.org.nrb.pars.auth.service.PasswordService;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.context.CurrentUserProvider;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
public class PasswordController {

    private final PasswordService passwordService;
    private final CurrentUserProvider currentUserProvider;

    public PasswordController(PasswordService passwordService, CurrentUserProvider currentUserProvider) {
        this.passwordService = passwordService;
        this.currentUserProvider = currentUserProvider;
    }

    @GetMapping("/forgot-password")
    public ApiResponse<ForgotPasswordStatusResponse> forgotPasswordStatus(@RequestParam String token) {
        return ApiResponse.ok(passwordService.forgotPasswordStatus(token));
    }

    @PostMapping("/forgot-password")
    public ApiResponse<Void> forgotPassword(@Valid @RequestBody ForgotPasswordRequest request) {
        return ApiResponse.ok(passwordService.requestForgotPassword(request.email()), null);
    }

    @PostMapping("/reset-password")
    public ApiResponse<Void> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        passwordService.resetPassword(request);
        return ApiResponse.ok("Password reset", null);
    }

    @PostMapping("/change-password")
    public ApiResponse<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        passwordService.changePassword(currentUserProvider.require(), request);
        return ApiResponse.ok("Password changed", null);
    }
}
