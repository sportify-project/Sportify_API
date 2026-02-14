package sport.store.thinh.domain.dto.response;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResAddressDTO {
    private long id;
    private long userId;
    private String fullName;
    private String phone;
    private String city;
    private String ward;
    private String addressLine1;
    private String addressLine2; // Không bắt buộc
    private boolean defaultAddress;
}
