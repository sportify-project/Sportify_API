package sport.store.thinh.domain.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
public class ResSupplierDTO {

    private Long id;
    private String name;
    private String phone;
    private String email;
    private String address;
    private String taxCode;
    private boolean active;
    private Instant createdAt;
    private String createdBy;
}
