package np.org.nrb.pars.ai.live;

import np.org.nrb.pars.ai.service.AiPermissionService;
import np.org.nrb.pars.common.context.CurrentUser;
import np.org.nrb.pars.deal.model.dto.DealResponse;
import np.org.nrb.pars.deal.service.DealService;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import np.org.nrb.pars.usermanagement.model.dto.UserResponse;
import np.org.nrb.pars.usermanagement.service.UserService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Read-only live PARS data access. Every lookup uses existing services and the
 * authenticated CurrentUser; no SQL or write operation is exposed to the model.
 */
@Service
@ConditionalOnProperty(prefix = "pars.ai", name = "enabled", havingValue = "true")
public class LiveDataService {

    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")
            .withZone(ZoneId.of("Asia/Kathmandu"));

    private final UserService userService;
    private final DealService dealService;
    private final AiPermissionService permissions;

    public LiveDataService(UserService userService, DealService dealService, AiPermissionService permissions) {
        this.userService = userService;
        this.dealService = dealService;
        this.permissions = permissions;
    }

    /** Facts about the authenticated user, read from PostgreSQL via the existing user service. */
    public String selfContext() {
        CurrentUser current = permissions.currentUser();
        UserResponse user = userService.get(current.userId());
        StringBuilder facts = new StringBuilder();
        facts.append("Authenticated PARS user (from database):\n");
        facts.append("username: ").append(user.username()).append('\n');
        facts.append("full name: ").append(user.fullName()).append('\n');
        facts.append("email: ").append(user.email()).append('\n');
        facts.append("roles: ").append(String.join(", ", user.roles())).append('\n');
        facts.append("position: ").append(user.positionName() == null ? "not assigned" : user.positionName()).append('\n');
        facts.append("enabled: ").append(user.enabled()).append('\n');
        facts.append("locked: ").append(user.lockedUntil() != null && user.lockedUntil().isAfter(Instant.now())).append('\n');
        facts.append("last successful login: ").append(user.lastSuccessfulLoginAt() == null
                ? "unknown" : FMT.format(user.lastSuccessfulLoginAt())).append('\n');
        facts.append("permissions: ").append(String.join(", ", current.permissions())).append('\n');
        facts.append("session id: ").append(current.sessionId()).append('\n');
        return facts.toString();
    }

    /** Recent deals through the existing deal service; permission checked first. */
    public String dealsContext() {
        CurrentUser current = permissions.currentUser();
        if (!current.hasPermission(PermissionCodes.DEAL_READ)) {
            return "You do not have DEAL_READ permission, so deal data is not available to you.";
        }
        List<DealResponse> deals = dealService.list(0, 5, null, null).getContent();
        if (deals.isEmpty()) {
            return "No deals are currently recorded in PARS.";
        }
        StringBuilder facts = new StringBuilder("Recent deals from PARS (read-only):\n");
        for (DealResponse deal : deals) {
            facts.append("- document ").append(deal.documentNo())
                    .append(", status ").append(deal.docStatus() == null ? "n/a" : deal.docStatus())
                    .append(", amount ").append(deal.amount() == null ? "n/a" : deal.amount())
                    .append(", deal date ").append(deal.dealDate() == null ? "n/a" : FMT.format(deal.dealDate()))
                    .append('\n');
        }
        return facts.toString();
    }

    /**
     * Application usage facts derived from the actual implemented code and RBAC —
     * not invented institutional policy.
     */
    public String appGuideContext() {
        CurrentUser current = permissions.currentUser();
        boolean canWrite = current.hasPermission(PermissionCodes.DEAL_WRITE);
        return """
                Verified PARS application facts (from application source and RBAC):
                - Deals page route: /deals; create form route: /deals/new.
                - The "New deal" button is shown only to users with DEAL_WRITE permission.
                - Your DEAL_WRITE permission: %s.
                - Backend create endpoint: POST /api/v1/deals, permission DEAL_WRITE, request body DealRequest.
                - DealRequest requires dealTypeId; all other fields are optional.
                - To create a deal in the UI: open Deals, click "New deal", enter a deal type and any other values, then click "Create deal".
                - Editing: open a deal and click "Edit" (requires DEAL_WRITE). Deleting requires DEAL_WRITE and a confirmation dialog.
                - If you cannot see the New deal button, ask an administrator to grant DEAL_WRITE.
                """.formatted(canWrite);
    }
}
