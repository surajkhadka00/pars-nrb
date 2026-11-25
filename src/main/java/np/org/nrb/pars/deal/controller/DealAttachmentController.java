package np.org.nrb.pars.deal.controller;

import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.deal.model.dto.DealAttachmentResponse;
import np.org.nrb.pars.deal.service.DealAttachmentService;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/deal-attachments")
public class DealAttachmentController {

    private final DealAttachmentService attachmentService;

    public DealAttachmentController(DealAttachmentService attachmentService) {
        this.attachmentService = attachmentService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEAL_READ + "')")
    public ApiResponse<List<DealAttachmentResponse>> list(@RequestParam UUID dealId) {
        return ApiResponse.ok(attachmentService.list(dealId));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEAL_WRITE + "')")
    public ApiResponse<DealAttachmentResponse> upload(
            @RequestParam UUID dealId,
            @RequestParam("file") MultipartFile file
    ) {
        return ApiResponse.ok("Attachment uploaded", attachmentService.upload(dealId, file));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEAL_READ + "')")
    public ResponseEntity<Resource> download(@PathVariable UUID id, @RequestParam UUID dealId) {
        return attachmentService.download(dealId, id);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.DEAL_WRITE + "')")
    public ApiResponse<Void> delete(@PathVariable UUID id, @RequestParam UUID dealId) {
        attachmentService.delete(dealId, id);
        return ApiResponse.ok("Attachment deleted", null);
    }
}
