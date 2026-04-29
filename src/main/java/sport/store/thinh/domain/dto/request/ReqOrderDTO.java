package sport.store.thinh.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ReqOrderDTO {
    private Long userId;
    private Long addressId;
    private String notes;
    private List<ReqOrderItemDTO> orderitems;

    @Getter
    @Setter
    public static class ReqOrderItemDTO {
        private Long orderId;
        private Long variantId;
        private int quantity;
        private BigDecimal price;
    }
}
