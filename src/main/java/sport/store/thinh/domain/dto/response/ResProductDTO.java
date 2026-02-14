package sport.store.thinh.domain.dto.response;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import sport.store.thinh.domain.dto.request.ReqProductDTO;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ResProductDTO {
    private Long productId;
    private String productName;
    private String slug;
    private Long brandId;
    private String brandName;
    private Long categoryId;
    private String categoryName;
    private String description;
    private String sku;
    private BigDecimal basePrice;
    private Integer stockQuantity;
    private String status;
    private List<ImageDTO> images;
    private List<VariantDTO> variants;

    @Getter
    @Setter
    public static class VariantDTO {
        private Long variantId;
        private String size;
        private String color;
        private String sku;
        private BigDecimal price;
        private int stockQuantity;
    }

    @Getter
    @Setter
    public static class ImageDTO {
        private Long imageId;
        private String url;
        private Integer displayOrder;
        private Boolean isPrimary;
    }
}
