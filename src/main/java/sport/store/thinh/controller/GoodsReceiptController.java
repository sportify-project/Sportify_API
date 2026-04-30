package sport.store.thinh.controller;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sport.store.thinh.domain.GoodsReceipt;
import sport.store.thinh.domain.dto.request.ReqGoodsReceiptDTO;
import sport.store.thinh.domain.dto.response.ResGoodsReceiptDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.service.GoodsReceiptService;
import sport.store.thinh.util.annotation.APIMessage;

@RestController
@RequestMapping("/api/v1")
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    public GoodsReceiptController(GoodsReceiptService goodsReceiptService) {
        this.goodsReceiptService = goodsReceiptService;
    }

    @PostMapping("/goods-receipts")
    @APIMessage("Create goods receipt")
    public ResponseEntity<ResGoodsReceiptDTO> handleCreate(@RequestBody ReqGoodsReceiptDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(goodsReceiptService.createReceipt(dto));
    }

    @GetMapping("/goods-receipts")
    @APIMessage("Get all goods receipts")
    public ResponseEntity<ResultPaginationDTO<ResGoodsReceiptDTO>> handleFindAll(
            @net.kaczmarzyk.spring.data.jpa.web.annotation.And({
                    @Spec(path = "status", spec = Equal.class),
                    @Spec(path = "supplier.id", params = "supplierId", spec = Equal.class)
            }) Specification<GoodsReceipt> spec,
            Pageable pageable) {
        return ResponseEntity.ok(goodsReceiptService.findAll(spec, pageable));
    }

    @GetMapping("/goods-receipts/{id}")
    @APIMessage("Get goods receipt by id")
    public ResponseEntity<ResGoodsReceiptDTO> handleFindById(@PathVariable Long id) {
        return ResponseEntity.ok(goodsReceiptService.findById(id));
    }

    @PutMapping("/goods-receipts/{id}")
    @APIMessage("Update goods receipt")
    public ResponseEntity<ResGoodsReceiptDTO> handleUpdate(@PathVariable Long id,
                                                           @RequestBody ReqGoodsReceiptDTO dto) {
        return ResponseEntity.ok(goodsReceiptService.updateReceipt(id, dto));
    }

    @PostMapping("/goods-receipts/{id}/confirm")
    @APIMessage("Confirm goods receipt")
    public ResponseEntity<ResGoodsReceiptDTO> handleConfirm(@PathVariable Long id) {
        return ResponseEntity.ok(goodsReceiptService.confirmReceipt(id));
    }

    @PostMapping("/goods-receipts/{id}/cancel")
    @APIMessage("Cancel goods receipt")
    public ResponseEntity<ResGoodsReceiptDTO> handleCancel(@PathVariable Long id) {
        return ResponseEntity.ok(goodsReceiptService.cancelReceipt(id));
    }
}
