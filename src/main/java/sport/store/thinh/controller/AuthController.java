package sport.store.thinh.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import sport.store.thinh.domain.Users;
import sport.store.thinh.domain.dto.request.ReqLoginDTO;
import sport.store.thinh.domain.dto.request.ReqRegisterDTO;
import sport.store.thinh.domain.dto.response.ResLoginDTO;
import sport.store.thinh.domain.dto.response.ResUserDTO;
import sport.store.thinh.service.UserService;
import sport.store.thinh.util.SecurityUtil;
import sport.store.thinh.util.annotation.APIMessage;
import sport.store.thinh.util.error.IdInvalidException;
import jakarta.validation.Valid;

import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/v1")
public class AuthController {
    private final AuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @Value("${thinh.jwt.refresh-token-validity-in-seconds}")
    private long refreshTokenExpiration;

    public AuthController(AuthenticationManager authenticationManager, SecurityUtil securityUtil, UserService userService, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.securityUtil = securityUtil;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/auth/register")
    @APIMessage("Register new user")
    public ResponseEntity<ResUserDTO> register(@Valid @RequestBody ReqRegisterDTO registerDTO) throws IdInvalidException {
        boolean isEmailExist = userService.existsByEmail(registerDTO.getEmail());
        if (isEmailExist) {
            throw new IdInvalidException("Email " + registerDTO.getEmail() + " đã tồn tại!");
        }

        Users createdUser = userService.registerUser(registerDTO);
        return ResponseEntity.status(201).body(userService.mapToUserDTO(createdUser));
    }

    @PostMapping("/auth/login")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO reqLoginDTO) {
        //Nạp input gồm username/password vào Security
        UsernamePasswordAuthenticationToken authenticationToken
                = new UsernamePasswordAuthenticationToken(reqLoginDTO.getUsername(), reqLoginDTO.getPassword());

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        reqLoginDTO.getUsername(),
                        reqLoginDTO.getPassword()
                )
        );

        //Tạo token
        //Set thông tin đăng nhập vào context
        SecurityContextHolder.getContext().setAuthentication(authentication);

        //Tạo user
        ResLoginDTO resLoginDTO = new ResLoginDTO();
        Users currentUser = userService.handleGetUserByUserName(reqLoginDTO.getUsername());
        if (currentUser != null) {
            ResLoginDTO.UserLogin u = new ResLoginDTO.UserLogin(
                    currentUser.getUserId(),
                    currentUser.getEmail(),
                    currentUser.getName(),
                    currentUser.getRole() != null ? currentUser.getRole().getRole() : null);
            resLoginDTO.setUser(u);
        }
        String accessToken = this.securityUtil.createAccessToken(authentication.getName(), resLoginDTO);
        resLoginDTO.setAccessToken(accessToken);

        //create refresh token
        String refreshToken = this.securityUtil.createRefreshToken(reqLoginDTO.getUsername(), resLoginDTO);
        userService.updateUserToken(refreshToken, reqLoginDTO.getUsername());

        //Set cookies
        ResponseCookie responseCookie = ResponseCookie.from("refresh-token", refreshToken)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(resLoginDTO);
    }

    @GetMapping("/auth/info")
    @APIMessage("Get refresh token")
    public ResponseEntity<ResLoginDTO> getRefreshToken(@CookieValue(name = "refresh-token", defaultValue = "idk") String refreshToken
    ) {
        if (refreshToken.equals("idk")) {
            throw new NoSuchElementException("Bạn không có refresh token");
        }
        //check valid
        Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
        String email = decodedToken.getSubject();

        //Check valid by email and token
        Users currentUser = userService.getUserByRefreshTokenAndEmail(refreshToken, email);
        if (currentUser == null) {
            throw new NoSuchElementException("Refresh token not found");
        }

        ResLoginDTO resLoginDTO = new ResLoginDTO();
        Users currentUserDB = userService.handleGetUserByUserName(email);
        if (currentUserDB != null) {
            ResLoginDTO.UserLogin u = new ResLoginDTO.UserLogin(
                    currentUserDB.getUserId(),
                    currentUserDB.getEmail(),
                    currentUserDB.getName(),
                    currentUserDB.getRole() != null ? currentUserDB.getRole().getRole() : null);
            resLoginDTO.setUser(u);
        }
        String accessToken = this.securityUtil.createAccessToken(email, resLoginDTO);
        resLoginDTO.setAccessToken(accessToken);

        //create refresh token
        String new_refreshToken = this.securityUtil.createRefreshToken(email, resLoginDTO);
        userService.updateUserToken(refreshToken, email);

        //Set cookies
        ResponseCookie responseCookie = ResponseCookie.from("refresh-token", new_refreshToken).httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .build();
        return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, responseCookie.toString()).body(resLoginDTO);
    }

    @PostMapping("/auth/logout")
    @APIMessage("Logout")
    public ResponseEntity<Void> logout(
            @CookieValue(name = "refresh-token", defaultValue = "") String refreshToken
    ) {
        // Xoá refresh token trong database (nếu có)
        if (!refreshToken.isEmpty()) {
            try {
                Jwt decodedToken = this.securityUtil.checkValidRefreshToken(refreshToken);
                String email = decodedToken.getSubject();
                userService.updateUserToken(null, email);
            } catch (Exception e) {
                // Token đã hết hạn hoặc không hợp lệ, bỏ qua
            }
        }

        // Xoá cookie refresh-token trên browser bằng cách set maxAge = 0
        ResponseCookie deleteRefreshCookie = ResponseCookie.from("refresh-token", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefreshCookie.toString())
                .build();
    }
}
