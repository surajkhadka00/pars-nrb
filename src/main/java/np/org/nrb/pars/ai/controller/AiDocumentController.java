package np.org.nrb.pars.ai.controller;

import np.org.nrb.pars.ai.domain.AiDtos;
import np.org.nrb.pars.ai.service.AiDocumentService;
import np.org.nrb.pars.ai.service.AiPermissionService;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import jakarta.validation.Valid;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai/documents")
@ConditionalOnProperty(prefix = "pars.ai", name = "enabled", havingValue = "true")
public class AiDocumentController {

    private final AiDocumentService documents;
    private final AiPermissionService permissions;

    public AiDocumentController(AiDocumentService documents, AiPermissionService permissions) {
        this.documents = documents;
        this.permissions = permissions;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('AI_DOCUMENT_READ')")
    public ApiResponse<List<AiDtos.AiDocumentResponse>> index(@RequestParam(defaultValue = "0") int page,
                                                              @RequestParam(defaultValue = "20") int size) {
        List<AiDtos.AiDocumentResponse> items = documents.list(page, size);
        return ApiResponse.paged(items, meta(page, size, documents.count()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_READ')")
    public ApiResponse<AiDtos.AiDocumentResponse> show(@PathVariable java.util.UUID id) {
        return ApiResponse.ok(documents.get(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('AI_DOCUMENT_UPLOAD')")
    public ApiResponse<AiDtos.AiDocumentResponse> upload(
            @RequestPart("file") MultipartFile file,
            @Valid @RequestPart("request") AiDtos.AiDocumentRequest request) {
        return ApiResponse.ok(documents.upload(file, request, permissions.userId()));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_APPROVE')")
    public ApiResponse<AiDtos.AiDocumentResponse> publish(@PathVariable java.util.UUID id) {
        return ApiResponse.ok(documents.publish(id, permissions.userId()));
    }

    @PostMapping("/{id}/archive")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_APPROVE')")
    public ApiResponse<AiDtos.AiDocumentResponse> archive(@PathVariable java.util.UUID id) {
        return ApiResponse.ok(documents.archive(id));
    }

    @PostMapping("/{id}/reindex")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_REINDEX')")
    public ApiResponse<AiDtos.AiDocumentResponse> reindex(@PathVariable java.util.UUID id) {
        return ApiResponse.ok(documents.reindex(id));
    }

    @GetMapping("/{id}/jobs")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_READ')")
    public ApiResponse<List<AiDtos.AiJobResponse>> jobs(@PathVariable java.util.UUID id) {
        return ApiResponse.ok(documents.jobs(0, 50).stream()
                .filter(job -> job.documentId().equals(id)).toList());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('AI_DOCUMENT_DELETE')")
    public ApiResponse<Void> delete(@PathVariable java.util.UUID id) {
        documents.delete(id);
        return ApiResponse.ok(null);
    }

    static ApiMeta meta(int page, int size, long total) {
        return new ApiMeta(page, size, total, (int) Math.ceil((double) total / size), null);
    }
}
