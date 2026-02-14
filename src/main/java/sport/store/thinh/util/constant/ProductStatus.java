package sport.store.thinh.util.constant;

public enum ProductStatus {
    ACTIVE("Đang kinh doanh"), INACTIVE("Ngừng kinh doanh"), OUT_OF_STOCK("Hết hàng");

    private final String value;

    ProductStatus(String value) {
        this.value = value;
    }

    public String value() {
        return this.value;
    }

    public static ProductStatus fromValue(String value) {
        for (ProductStatus p : ProductStatus.values()) {
            if (p.value.equalsIgnoreCase(value)) {
                return p;
            }
        }
        return null;
    }
}
