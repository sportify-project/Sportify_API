package sport.store.thinh.domain.dto.response;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Getter
@Setter
public class ResGoodsReceiptDTO {

    private Long id;
    private ResSupplierDTO supplier;
    private String status;
    private BigDecimal totalCost;
    private String notes;
    private String createdBy;
    private Instant createdAt;
    private Instant confirmedAt;
    private List<ResGoodsReceiptItemDTO> items;

    @Getter
    @Setter
    public static class ResGoodsReceiptItemDTO {
        private Long id;
        private Long variantId;
        private String sku;
        private String productName;
        private String variantSize;
        private String variantColor;
        private int quantity;
        private BigDecimal costPrice;
        private BigDecimal subtotal;
    }
}
