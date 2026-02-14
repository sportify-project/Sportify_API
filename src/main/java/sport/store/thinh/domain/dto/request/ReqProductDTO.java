package sport.store.thinh.domain.dto.request;

import jakarta.persistence.Column;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
public class ReqProductDTO {
    private Long productId;
    @NotBlank(message = "Tên sản phẩm không được để trống")
    private String productName;
    @NotNull(message = "Thương hiệu không được để trống")
    private Long brandId;
    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
    private String description;
    private String sku;
    @DecimalMin("0.0")
    private BigDecimal basePrice;
    @Min(0)
    private Integer stockQuantity;
    private String status;
    private List<ImageDTO> images;
    private List<VariantDTO> variants;

    @Getter
    @Setter
    public static class VariantDTO {
        private Long variantId;
        @NotBlank(message = "Size không được trống")
        private String size;
        @NotBlank(message = "Màu không được trống")
        private String color;
        private String sku;
        private BigDecimal price;
        @Min(0)
        private int stockQuantity;
    }

    @Getter
    @Setter
    public static class ImageDTO {
        private Long imageId;
        @NotBlank(message = "URL ảnh không được trống")
        private String url;
        private Integer displayOrder;
        private Boolean isPrimary;
    }
}
