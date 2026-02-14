package sport.store.thinh.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import sport.store.thinh.domain.dto.response.RestResponse;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Optional;

@Component
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public CustomAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");

        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatus(HttpStatus.UNAUTHORIZED.value());
        restResponse.setError(
                Optional.ofNullable(authException.getCause())
                        .map(Throwable::getMessage)
                        .orElse(authException.getMessage())
        );
        restResponse.setMessage("Token không hợp lệ hoặc chưa đăng nhập");

        objectMapper.writeValue(response.getWriter(), restResponse);
    }
}
