package sport.store.thinh.controller;

import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sport.store.thinh.domain.Discount;
import sport.store.thinh.domain.dto.request.ReqDiscountDTO;
import sport.store.thinh.domain.dto.response.ResDiscountDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.service.DiscountService;
import sport.store.thinh.util.annotation.APIMessage;

@RestController
@RequestMapping("/api/v1")
public class DiscountController {

    private final DiscountService discountService;

    public DiscountController(DiscountService discountService) {
        this.discountService = discountService;
    }

    @PostMapping("/discounts")
    @APIMessage("Create a new discount")
    public ResponseEntity<ResDiscountDTO> handleCreate(@RequestBody ReqDiscountDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(discountService.create(dto));
    }

    @GetMapping("/discounts")
    @APIMessage("Get all discounts")
    public ResponseEntity<ResultPaginationDTO<ResDiscountDTO>> handleFindAll(
            @Spec(path = "variant.product.name", params = "name", spec = Like.class) Specification<Discount> spec,
            Pageable pageable) {
        return ResponseEntity.ok(discountService.findAll(spec, pageable));
    }

    @GetMapping("/discounts/{id}")
    @APIMessage("Get discount details")
    public ResponseEntity<ResDiscountDTO> handleFindById(@PathVariable Long id) {
        return ResponseEntity.ok(discountService.findById(id));
    }

    @PutMapping("/discounts/{id}")
    @APIMessage("Update discount")
    public ResponseEntity<ResDiscountDTO> handleUpdate(@PathVariable Long id, @RequestBody ReqDiscountDTO dto) {
        return ResponseEntity.ok(discountService.update(id, dto));
    }

    @DeleteMapping("/discounts/{id}")
    @APIMessage("Delete discount")
    public ResponseEntity<Void> handleDelete(@PathVariable Long id) {
        discountService.delete(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
