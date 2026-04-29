package sport.store.thinh.domain.dto.response;

import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;

@Getter
@Setter
public class ResProductVariantDTO {
    private Long id;
    private String sku;
    private String size;
    private String color;
    private BigDecimal price;
    private int stockQuantity;
    private ProductInnerDTO product;

    @Getter
    @Setter
    public static class ProductInnerDTO {
        private Long id;
        private String name;
    }
}
