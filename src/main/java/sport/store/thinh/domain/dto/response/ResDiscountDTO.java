package sport.store.thinh.domain.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ResDiscountDTO {
    private Long discountId;
    private int discountPercentage;
    private Long variantId;
    private String variantSku;
    private String productName;
    private Date startDate;
    private Date endDate;
    private int quantity;
}
