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
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
@RestController
@RequestMapping("/api/v1")
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @PostMapping(value = "/products", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @APIMessage("Create a new product")
    public ResponseEntity<ResProductDTO> createProduct(
            @RequestPart("product") @Valid ReqProductDTO reqProductDTO,
            @RequestPart(value = "file", required = false) MultipartFile file){
        return ResponseEntity.status(201).body(productService.createProduct(reqProductDTO, file));
    }

    @PutMapping(value = "/products", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE, MediaType.APPLICATION_JSON_VALUE})
    @APIMessage("Edit a product")
    public ResponseEntity<ResProductDTO> editProduct(
            @RequestPart("product") @Valid ReqProductDTO reqProductDTO,
            @RequestPart(value = "file", required = false) MultipartFile file){
        return ResponseEntity.ok(productService.editProduct(reqProductDTO, file));
    }

    @GetMapping("/products")
    @APIMessage("Get all products")
    public ResponseEntity<ResultPaginationDTO<ResProductDTO>> getAllProducts(@Spec(path = "name", spec = Like.class) Specification<Product> spec, Pageable pageable){
        return ResponseEntity.ok().body(productService.getAllProducts(spec, pageable));
    }

    @GetMapping("/products/{id}")
    @APIMessage("Get product by id")
    public ResponseEntity<ResProductDTO> getProductById(@PathVariable Long id){

    }

}
