package sport.store.thinh.util;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;
import sport.store.thinh.domain.dto.response.RestResponse;
import sport.store.thinh.util.annotation.APIMessage;

@ControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
            Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
            ServerHttpResponse response) {
        HttpServletResponse servletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int status = servletResponse.getStatus();

        RestResponse<Object> restResponse = new RestResponse<>();
        if (body instanceof String) {
            return body;
        }
        if (status >= 400) {
            return body;
        } else {
            // case success
            APIMessage apiMessage = returnType.getMethodAnnotation(APIMessage.class);
            restResponse.setData(body);
            restResponse.setMessage(apiMessage != null ? apiMessage.value() : "CALL API SUCCESSFULLY");
        }

        String path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return body;
        }

        restResponse.setStatus(status);
        return restResponse;

    }
}
