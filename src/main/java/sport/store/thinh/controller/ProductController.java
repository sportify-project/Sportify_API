package sport.store.thinh.controller;

import jakarta.validation.Valid;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sport.store.thinh.domain.Product;
import sport.store.thinh.domain.dto.request.ReqProductDTO;
import sport.store.thinh.domain.dto.response.ResProductDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.service.ProductService;
import sport.store.thinh.util.annotation.APIMessage;

@RestController
@RequestMapping("/api/v1")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping("/products")
    @APIMessage("Create a new product")
    public ResponseEntity<ResProductDTO> createProduct(@Valid @RequestBody ReqProductDTO reqProductDTO){
        return ResponseEntity.status(201).body(productService.createProduct(reqProductDTO));
    }

    @PutMapping("/products")
    @APIMessage("Edit a product")
    public ResponseEntity<ResProductDTO> editProduct(@Valid @RequestBody ReqProductDTO reqProductDTO){
        return ResponseEntity.ok(productService.editProduct(reqProductDTO));
    }

    @GetMapping("/products")
    @APIMessage("Get all products")
    public ResponseEntity<ResultPaginationDTO<ResProductDTO>> getAllProducts(@Spec(path = "name", spec = Like.class) Specification<Product> spec, Pageable pageable){
        return ResponseEntity.ok().body(productService.getAllProducts(spec, pageable));
    }
}
