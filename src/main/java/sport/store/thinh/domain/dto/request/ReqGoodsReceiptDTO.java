package sport.store.thinh.domain.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ReqGoodsReceiptDTO {

    private Long supplierId;
    private String notes;
    private List<ReqGoodsReceiptItemDTO> items;

    @Getter
    @Setter
    public static class ReqGoodsReceiptItemDTO {
        private Long variantId;
        private int quantity;
        private BigDecimal costPrice;
    }
}
