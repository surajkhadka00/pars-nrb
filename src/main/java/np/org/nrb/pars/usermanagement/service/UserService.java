package np.org.nrb.pars.usermanagement.service;

import np.org.nrb.pars.common.api.OptionItem;
import np.org.nrb.pars.common.exception.Exceptions;
import np.org.nrb.pars.common.query.ODataQuery;
import np.org.nrb.pars.rbac.model.Role;
import np.org.nrb.pars.rbac.repository.RoleRepository;
import np.org.nrb.pars.rbac.service.PermissionResolver;
import np.org.nrb.pars.usermanagement.model.AppUser;
import np.org.nrb.pars.usermanagement.model.Position;
import np.org.nrb.pars.usermanagement.model.dto.CreateUserRequest;
import np.org.nrb.pars.usermanagement.model.dto.UpdateUserRequest;
import np.org.nrb.pars.usermanagement.model.dto.UserResponse;
import np.org.nrb.pars.usermanagement.repository.AppUserRepository;
import np.org.nrb.pars.usermanagement.repository.PositionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

@Service
public class UserService {

    private static final int MAX_IMAGE_BYTES = 2 * 1024 * 1024;

    private final AppUserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PositionRepository positionRepository;
    private final PasswordEncoder passwordEncoder;
    private final PermissionResolver permissionResolver;
    private final ODataQuery oDataQuery;

    public UserService(
            AppUserRepository userRepository,
            RoleRepository roleRepository,
            PositionRepository positionRepository,
            PasswordEncoder passwordEncoder,
            PermissionResolver permissionResolver,
            ODataQuery oDataQuery
    ) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.positionRepository = positionRepository;
        this.passwordEncoder = passwordEncoder;
        this.permissionResolver = permissionResolver;
        this.oDataQuery = oDataQuery;
    }

    @Transactional(readOnly = true)
    public Page<UserResponse> list(int page, int size, String sort, String filter) {
        Pageable pageable = oDataQuery.pageable(AppUser.class, page, size, sort);
        return oDataQuery.findAll(userRepository, AppUser.class, filter, pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public List<OptionItem> options() {
        return userRepository.findAll().stream()
                .map(user -> new OptionItem(user.getId(), user.getFullName() + " (" + user.getUsername() + ")"))
                .toList();
    }

    @Transactional(readOnly = true)
    public UserResponse get(UUID id) {
        return toResponse(load(id));
    }

    @Transactional
    public UserResponse create(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw Exceptions.conflict("Username already exists");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw Exceptions.conflict("Email already exists");
        }
        AppUser user = new AppUser();
        user.setUsername(request.username());
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setMobile(blankToNull(request.mobile()));
        user.setAddress(blankToNull(request.address()));
        user.setPosition(resolvePosition(request.positionId()));
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setPasswordChangedAt(Instant.now());
        user.setRoles(resolveRoles(request.roleIds()));
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(UUID id, UpdateUserRequest request) {
        AppUser user = load(id);
        if (!Objects.equals(user.getVersion(), request.version())) {
            throw Exceptions.optimisticLock();
        }
        if (!user.getEmail().equalsIgnoreCase(request.email()) && userRepository.existsByEmail(request.email())) {
            throw Exceptions.conflict("Email already exists");
        }
        user.setEmail(request.email());
        user.setFullName(request.fullName());
        user.setMobile(blankToNull(request.mobile()));
        user.setAddress(blankToNull(request.address()));
        user.setPosition(resolvePosition(request.positionId()));
        user.setEnabled(request.enabled());
        Set<Role> newRoles = resolveRoles(request.roleIds());
        // ADMIN is never removed from a user who already has it.
        boolean currentlyAdmin = user.getRoles().stream().anyMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()));
        if (currentlyAdmin && newRoles.stream().noneMatch(r -> "ADMIN".equalsIgnoreCase(r.getCode()))) {
            user.getRoles().stream().filter(r -> "ADMIN".equalsIgnoreCase(r.getCode())).findFirst()
                    .ifPresent(newRoles::add);
        }
        user.setRoles(newRoles);
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse lock(UUID id) {
        AppUser user = load(id);
        user.setLockedAt(Instant.now());
        user.setLockedUntil(Instant.now().plusSeconds(365L * 24 * 3600));
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse unlock(UUID id) {
        AppUser user = load(id);
        user.setLockedAt(null);
        user.setLockedUntil(null);
        user.setFailedCount(0);
        user.setExpired(false);
        user.setEnabled(true);
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse uploadImage(UUID id, MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw Exceptions.badRequest("Image file is required");
        }
        if (file.getSize() > MAX_IMAGE_BYTES) {
            throw Exceptions.badRequest("Image must be 2 MB or smaller");
        }
        byte[] bytes;
        try {
            bytes = file.getBytes();
        } catch (Exception ex) {
            throw Exceptions.badRequest("Image could not be read");
        }
        String contentType = imageContentType(bytes);
        AppUser user = load(id);
        user.setImage(bytes);
        user.setImageContentType(contentType);
        userRepository.save(user);
        return toResponse(user);
    }

    @Transactional(readOnly = true)
    public AppUser image(UUID id) {
        AppUser user = load(id);
        if (user.getImage() == null || user.getImage().length == 0) {
            throw Exceptions.notFound("User image not found");
        }
        return user;
    }

    private AppUser load(UUID id) {
        return userRepository.findWithRolesById(id).orElseThrow(() -> Exceptions.notFound("User not found"));
    }

    private Set<Role> resolveRoles(Set<UUID> roleIds) {
        if (roleIds == null || roleIds.isEmpty()) {
            Role userRole = roleRepository.findByCode("USER")
                    .orElseThrow(() -> Exceptions.notFound("USER role is not configured"));
            return new HashSet<>(Set.of(userRole));
        }
        List<Role> roles = roleRepository.findAllById(roleIds);
        if (roles.size() != roleIds.size()) {
            throw Exceptions.badRequest("One or more roles were not found");
        }
        return new HashSet<>(roles);
    }

    private Position resolvePosition(UUID positionId) {
        if (positionId == null) {
            return null;
        }
        return positionRepository.findById(positionId)
                .orElseThrow(() -> Exceptions.badRequest("Position was not found"));
    }

    private static String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }

    private static String imageContentType(byte[] bytes) {
        if (startsWith(bytes, new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF})) {
            return "image/jpeg";
        }
        if (startsWith(bytes, new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47})) {
            return "image/png";
        }
        if (startsWith(bytes, new byte[]{0x47, 0x49, 0x46, 0x38})) {
            return "image/gif";
        }
        if (bytes.length >= 12
                && startsWith(bytes, new byte[]{0x52, 0x49, 0x46, 0x46})
                && bytes[8] == 0x57 && bytes[9] == 0x45 && bytes[10] == 0x42 && bytes[11] == 0x50) {
            return "image/webp";
        }
        throw Exceptions.badRequest("Image must be JPEG, PNG, GIF, or WEBP");
    }

    private static boolean startsWith(byte[] bytes, byte[] prefix) {
        if (bytes.length < prefix.length) {
            return false;
        }
        for (int i = 0; i < prefix.length; i++) {
            if (bytes[i] != prefix[i]) {
                return false;
            }
        }
        return true;
    }

    private UserResponse toResponse(AppUser user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getMobile(),
                user.getAddress(),
                user.getPosition() == null ? null : user.getPosition().getId(),
                user.getPosition() == null ? null : user.getPosition().getName(),
                user.getImageContentType() != null,
                user.isEnabled(),
                user.isExpired(),
                user.getLockedUntil(),
                user.getLastSuccessfulLoginAt(),
                user.getInactiveExpireAt(),
                permissionResolver.roleCodes(user),
                user.getVersion()
        );
    }
}
