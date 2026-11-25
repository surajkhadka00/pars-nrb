package np.org.nrb.pars.ai.service;

import np.org.nrb.pars.common.context.CurrentUser;
import np.org.nrb.pars.common.context.CurrentUserProvider;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * Derives all authorization inputs from the authenticated JWT subject.
 * The browser never supplies a user id, role list or permission list.
 */
@Service
public class AiPermissionService {

    private final CurrentUserProvider currentUserProvider;

    public AiPermissionService(CurrentUserProvider currentUserProvider) {
        this.currentUserProvider = currentUserProvider;
    }

    public CurrentUser currentUser() {
        return currentUserProvider.require();
    }

    public java.util.UUID userId() {
        return currentUser().userId();
    }

    /** Metadata values that this user is allowed to retrieve. */
    public List<String> accessTokens() {
        CurrentUser user = currentUser();
        List<String> tokens = new ArrayList<>();
        tokens.add("PUBLIC");
        tokens.add("INTERNAL");
        user.roles().forEach(role -> tokens.add("ROLE_" + role));
        tokens.add("USER_" + user.userId());
        return tokens;
    }

    /** Metadata value stored on documents that this user must never see. */
    public String denyToken() {
        return currentUser().userId().toString();
    }

    public boolean hasRole(String role) {
        return currentUser().roles().contains(role);
    }
}
