package sport.store.thinh.util.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import sport.store.thinh.domain.dto.response.RestResponse;

import java.util.NoSuchElementException;

@ControllerAdvice
public class GlobalException{
    //Handle all exception
    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestResponse<Object>> handleAllException(Exception ex) {
        RestResponse<Object> res = new RestResponse<Object>();
        res.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        res.setMessage(ex.getMessage());
        res.setError("Internal Server Error");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(res);
    }

    @ExceptionHandler(value = {
            BadCredentialsException.class,
            NoSuchElementException.class,
            IllegalArgumentException.class,
            IdInvalidException.class
    })
    public ResponseEntity<RestResponse<Object>> handleGlobalException(Exception e) {
        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setMessage(e.getMessage());
        restResponse.setError("Exception occurs");
        restResponse.setStatus(HttpStatus.BAD_REQUEST.value());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(restResponse);
    }
}
