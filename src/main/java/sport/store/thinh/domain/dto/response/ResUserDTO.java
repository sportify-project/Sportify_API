package sport.store.thinh.domain.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResUserDTO {
    private long id;
    private String username;
    private String email;
    private String phone;
    private String gender;
    private int age;
    private Instant createdAt;
    private String createdBy;
    private Instant updatedAt;
    private String updatedBy;
}
