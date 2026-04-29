package sport.store.thinh.domain.dto.response;

import lombok.Getter;
import lombok.Setter;
import sport.store.thinh.domain.OrderItem;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ResOrderDTO {
    private Long orderId;
    private Long userId;
    private String address;
    private BigDecimal total;

    List<ResOrderItemDTO> orderItems;

    @Getter
    @Setter
    public static class ResOrderItemDTO {
        private Long orderId;
        private Long variantId;
        private String variantColor;
        private String variantSize;
        private String productName;
        private String productImageUrl;
        private int quantity;
        private BigDecimal price;
    }
}
