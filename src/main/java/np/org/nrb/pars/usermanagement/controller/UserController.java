package np.org.nrb.pars.usermanagement.controller;

import jakarta.validation.Valid;
import np.org.nrb.pars.common.api.ApiMeta;
import np.org.nrb.pars.common.api.ApiResponse;
import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.rbac.model.PermissionCodes;
import np.org.nrb.pars.usermanagement.model.AppUser;
import np.org.nrb.pars.usermanagement.model.dto.CreateUserRequest;
import np.org.nrb.pars.usermanagement.model.dto.UpdateUserRequest;
import np.org.nrb.pars.usermanagement.model.dto.UserResponse;
import np.org.nrb.pars.usermanagement.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.USER_READ + "')")
    public ApiResponse<List<UserResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String sort,
            @RequestParam(name = "$filter", required = false) String filter
    ) {
        Page<UserResponse> result = userService.list(page, size, sort, filter);
        return ApiResponse.paged(result.getContent(), ApiMeta.from(result));
    }

    @GetMapping("/options")
    @PreAuthorize("hasAuthority('" + PermissionCodes.USER_READ + "')")
    public ApiResponse<List<OptionItem>> options() {
        return ApiResponse.ok(userService.options());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.USER_READ + "')")
    public ApiResponse<UserResponse> get(@PathVariable UUID id) {
        return ApiResponse.ok(userService.get(id));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + PermissionCodes.USER_WRITE + "')")
    public ApiResponse<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {
        return ApiResponse.ok("User created", userService.create(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + PermissionCodes.USER_WRITE + "')")
    public ApiResponse<UserResponse> update(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return ApiResponse.ok("User updated", userService.update(id, request));
    }

    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + PermissionCodes.USER_WRITE + "')")
    public ApiResponse<UserResponse> uploadImage(@PathVariable UUID id, @RequestPart("file") MultipartFile file) {
        return ApiResponse.ok("User image uploaded", userService.uploadImage(id, file));
    }

    @GetMapping("/{id}/image")
    @PreAuthorize("hasAuthority('" + PermissionCodes.USER_READ + "')")
    public ResponseEntity<byte[]> image(@PathVariable UUID id) {
        AppUser user = userService.image(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE, user.getImageContentType())
                .body(user.getImage());
    }

    @PostMapping("/{id}/lock")
    @PreAuthorize("hasAuthority('" + PermissionCodes.USER_WRITE + "')")
    public ApiResponse<UserResponse> lock(@PathVariable UUID id) {
        return ApiResponse.ok("User locked", userService.lock(id));
    }

    @PostMapping("/{id}/unlock")
    @PreAuthorize("hasAuthority('" + PermissionCodes.USER_WRITE + "')")
    public ApiResponse<UserResponse> unlock(@PathVariable UUID id) {
        return ApiResponse.ok("User unlocked", userService.unlock(id));
    }
}
