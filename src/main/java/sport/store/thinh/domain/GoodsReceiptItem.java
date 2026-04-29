package sport.store.thinh.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Entity
@Table(name = "goods_receipt_items")
@Getter
@Setter
@NoArgsConstructor
public class GoodsReceiptItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_variant_id", nullable = false)
    private ProductVariant productVariant;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "cost_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal costPrice;

    @Column(nullable = false, precision = 15, scale = 2)
    private BigDecimal subtotal;

    /**
     * Tính subtotal tự động khi set quantity hoặc costPrice
     */
    public void calculateSubtotal() {
        if (this.costPrice != null && this.quantity > 0) {
            this.subtotal = this.costPrice.multiply(BigDecimal.valueOf(this.quantity));
        }
    }
}
