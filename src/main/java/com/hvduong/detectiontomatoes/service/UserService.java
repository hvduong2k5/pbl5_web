package com.hvduong.detectiontomatoes.service;

import com.hvduong.detectiontomatoes.model.dto.UserCreateDTO;
import com.hvduong.detectiontomatoes.model.dto.UserResponseDTO;
import com.hvduong.detectiontomatoes.model.dto.UserRolesUpdateDTO;
import com.hvduong.detectiontomatoes.model.dto.UserUpdateDTO;
import com.hvduong.detectiontomatoes.model.entity.Role;
import com.hvduong.detectiontomatoes.model.entity.User;
import com.hvduong.detectiontomatoes.repository.RoleRepository;
import com.hvduong.detectiontomatoes.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CacheManager cacheManager;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, CacheManager cacheManager) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.cacheManager = cacheManager;
    }

    private UserResponseDTO mapToResponse(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .enabled(user.getEnabled())
                .createdAt(user.getCreatedAt())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .build();
    }

    public Page<UserResponseDTO> getAllUsers(String keyword, Pageable pageable) {
        if (keyword != null && !keyword.trim().isEmpty()) {
            return userRepository.findByUsernameContainingIgnoreCase(keyword.trim(), pageable).map(this::mapToResponse);
        }
        return userRepository.findAll(pageable).map(this::mapToResponse);
    }

    @Transactional
    public UserResponseDTO createUser(UserCreateDTO dto) {
        if (userRepository.findByUsername(dto.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Tài khoản đã tồn tại trong hệ thống.");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setEnabled(dto.getEnabled() != null ? dto.getEnabled() : true);

        Set<Role> roles = new HashSet<>();
        if (dto.getRoles() != null && !dto.getRoles().isEmpty()) {
            for (String roleName : dto.getRoles()) {
                roleRepository.findByName(roleName.toUpperCase()).ifPresent(roles::add);
            }
        }
        user.setRoles(roles);

        User savedUser = userRepository.save(user);
        return mapToResponse(savedUser);
    }

    @Transactional
    public UserResponseDTO updateUser(Integer id, UserUpdateDTO dto, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));

        boolean isSelf = user.getUsername().equals(currentUsername);

        if (dto.getPassword() != null && !dto.getPassword().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(dto.getPassword()));
        }

        if (dto.getEnabled() != null) {
            if (isSelf && !dto.getEnabled()) {
                throw new IllegalArgumentException("Bạn không thể tự khóa (disable) tài khoản của chính mình.");
            }
            user.setEnabled(dto.getEnabled());
        }

        if (dto.getRoles() != null) {
            if (isSelf) {
                boolean hasAdminRole = dto.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
                if (!hasAdminRole) {
                    throw new IllegalArgumentException("Bạn không thể tự xóa quyền ADMIN của chính mình.");
                }
            }
            Set<Role> roles = new HashSet<>();
            for (String roleName : dto.getRoles()) {
                roleRepository.findByName(roleName.toUpperCase()).ifPresent(roles::add);
            }
            user.setRoles(roles);
        }

        User updatedUser = userRepository.save(user);
        if (cacheManager.getCache("userDetails") != null) {
            cacheManager.getCache("userDetails").evict(updatedUser.getUsername());
        }
        return mapToResponse(updatedUser);
    }

    @Transactional
    public UserResponseDTO updateUserRoles(Integer id, UserRolesUpdateDTO dto, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));

        boolean isSelf = user.getUsername().equals(currentUsername);

        if (isSelf) {
            boolean hasAdminRole = dto.getRoles().stream().anyMatch(r -> r.equalsIgnoreCase("ADMIN"));
            if (!hasAdminRole) {
                throw new IllegalArgumentException("Bạn không thể tự xóa quyền ADMIN của chính mình.");
            }
        }

        Set<Role> roles = new HashSet<>();
        for (String roleName : dto.getRoles()) {
            roleRepository.findByName(roleName.toUpperCase()).ifPresent(roles::add);
        }
        user.setRoles(roles);

        User updatedUser = userRepository.save(user);
        if (cacheManager.getCache("userDetails") != null) {
            cacheManager.getCache("userDetails").evict(updatedUser.getUsername());
        }
        return mapToResponse(updatedUser);
    }

    @Transactional
    public void deleteUser(Integer id, String currentUsername) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + id));
        
        if (user.getUsername().equals(currentUsername)) {
            throw new IllegalArgumentException("Bạn không thể tự xóa tài khoản của chính mình.");
        }

        userRepository.deleteById(id);
        if (cacheManager.getCache("userDetails") != null) {
            cacheManager.getCache("userDetails").evict(user.getUsername());
        }
    }
}
