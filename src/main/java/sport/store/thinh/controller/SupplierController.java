package sport.store.thinh.controller;

import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sport.store.thinh.domain.Supplier;
import sport.store.thinh.domain.dto.request.ReqSupplierDTO;
import sport.store.thinh.domain.dto.response.ResSupplierDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.service.SupplierService;
import sport.store.thinh.util.annotation.APIMessage;

@RestController
@RequestMapping("/api/v1")
public class SupplierController {

    private final SupplierService supplierService;

    public SupplierController(SupplierService supplierService) {
        this.supplierService = supplierService;
    }

    @PostMapping("/suppliers")
    @APIMessage("Create supplier")
    public ResponseEntity<ResSupplierDTO> handleCreate(@RequestBody ReqSupplierDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(supplierService.create(dto));
    }

    @GetMapping("/suppliers")
    @APIMessage("Get all suppliers")
    public ResponseEntity<ResultPaginationDTO<ResSupplierDTO>> handleFindAll(
            @Spec(path = "name", spec = Like.class) Specification<Supplier> spec,
            Pageable pageable) {
        return ResponseEntity.ok(supplierService.findAll(spec, pageable));
    }

    @GetMapping("/suppliers/{id}")
    @APIMessage("Get supplier by id")
    public ResponseEntity<ResSupplierDTO> handleFindById(@PathVariable Long id) {
        return ResponseEntity.ok(supplierService.findById(id));
    }

    @PutMapping("/suppliers/{id}")
    @APIMessage("Update supplier")
    public ResponseEntity<ResSupplierDTO> handleUpdate(@PathVariable Long id,
                                                       @RequestBody ReqSupplierDTO dto) {
        return ResponseEntity.ok(supplierService.update(id, dto));
    }

    @DeleteMapping("/suppliers/{id}")
    @APIMessage("Deactivate supplier")
    public ResponseEntity<Void> handleDeactivate(@PathVariable Long id) {
        supplierService.deactivate(id);
        return ResponseEntity.ok(null);
    }
}
