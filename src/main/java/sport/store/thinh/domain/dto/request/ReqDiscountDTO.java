package sport.store.thinh.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
public class ReqDiscountDTO {
    private int discountPercentage;
    private Long variantId;
    private Date startDate;
    private Date endDate;
    private int quantity;
}
