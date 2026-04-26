package sport.store.thinh.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import sport.store.thinh.domain.Roles;
import sport.store.thinh.domain.Users;
import sport.store.thinh.domain.dto.request.ReqRegisterDTO;
import sport.store.thinh.domain.dto.response.ResUserDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.repository.RoleRepository;
import sport.store.thinh.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResUserDTO findById(long id) {
        Optional<Users> usersOptional = userRepository.findById(id);
        ResUserDTO userDTO = new ResUserDTO();
        if (usersOptional.isPresent()) {
            Users user = usersOptional.get();
            return mapToUserDTO(user);
        } else
            throw new NoSuchElementException("Không tồn tại user nào có id là " + id);
    }

    public ResultPaginationDTO<ResUserDTO> findAllUsers(Specification<Users> spec, Pageable pageable) {
        Page<Users> usersPage;
        if (spec == null) {
            usersPage = userRepository.findAll(pageable);
        } else {
            usersPage = userRepository.findAll(spec, pageable);
        }
        List<ResUserDTO> userDTOList = usersPage.getContent().stream().map(this::mapToUserDTO).toList();
        ResultPaginationDTO<ResUserDTO> resultPaginationDTO = new ResultPaginationDTO<>();

        resultPaginationDTO.setResult(userDTOList);
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(usersPage.getTotalElements());
        meta.setPages(usersPage.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(userDTOList);
        return resultPaginationDTO;
    }

    public Users createUser(Users user) {
        return userRepository.save(user);
    }

    public Users registerUser(ReqRegisterDTO dto) {
        Users user = new Users();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(this.passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setGender(dto.getGender());
        user.setAge(dto.getAge());
        user.setStatus("Active");

        // Gán role: Ưu tiên từ DTO, nếu không có thì mặc định là USER
        String roleName = (dto.getRole() != null && !dto.getRole().isEmpty()) ? dto.getRole() : "USER";
        Roles userRole = this.roleRepository.findByRole(roleName);
        if (userRole != null) {
            user.setRole(userRole);
        }

        return userRepository.save(user);
    }

    public ResUserDTO mapToUserDTO(Users user) {
        ResUserDTO userDTO = new ResUserDTO();
        userDTO.setId(user.getUserId());
        userDTO.setUsername(user.getName());
        userDTO.setEmail(user.getEmail());
        userDTO.setAge(user.getAge());
        userDTO.setGender(user.getGender());
        userDTO.setPhone(user.getPhone());
        userDTO.setStatus(user.getStatus());
        return userDTO;
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    public void updateUserToken(String token, String email) {
        Users currentUser = this.userRepository.findByEmail(email);
        if (currentUser != null) {
            currentUser.setRefreshToken(token);
            userRepository.save(currentUser);
        }
    }

    public Users handleGetUserByUserName(String username) {
        return this.userRepository.findByEmail(username);
    }

    public ResUserDTO handlegetUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        if (this.userRepository.existsByEmail(email)) {
            return this.mapToUserDTO(this.userRepository.findByEmail(email));
        } else {
            return null;
        }
    }

    public Users getUserByRefreshTokenAndEmail(String refreshToken, String email) {
        return this.userRepository.findByRefreshTokenAndEmail(refreshToken, email);
    }
}
