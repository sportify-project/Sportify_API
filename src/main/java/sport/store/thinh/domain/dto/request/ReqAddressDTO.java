package sport.store.thinh.domain.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReqAddressDTO {

    @NotNull(message = "Người dùng không được để trống")
    private Long userId;

    @NotBlank(message = "Tên người nhận không được để trống")
    private String fullName;

    @NotBlank(message = "Số điện thoại không được để trống")
    @Pattern(regexp = "^[0-9]{10}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @NotBlank(message = "Vui lòng chọn Tỉnh/Thành phố")
    private String city;

    @NotBlank(message = "Vui lòng chọn Phường/Xã")
    private String ward;

    @NotBlank(message = "Vui lòng nhập số nhà, tên đường")
    private String addressLine1;

    private String addressLine2; // Không bắt buộc

    private boolean defaultAddress;
}
