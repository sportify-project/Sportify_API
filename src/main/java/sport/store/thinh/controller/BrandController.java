package sport.store.thinh.controller;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sport.store.thinh.domain.Brand;
import sport.store.thinh.domain.Users;
import sport.store.thinh.domain.dto.response.ResBrandDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.service.BrandService;
import sport.store.thinh.util.annotation.APIMessage;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class BrandController {
    private final BrandService brandService;

    public BrandController(BrandService brandService) {
        this.brandService = brandService;
    }

    @PostMapping("/brands")
    @APIMessage("Create new brand")
    public ResponseEntity<ResBrandDTO> handleCreateBrand(@RequestBody Brand brand) {
        return ResponseEntity.status(HttpStatus.CREATED).body(brandService.createBrand(brand));
    }

    @GetMapping("/brands")
    @APIMessage("Get all brands")
    public ResponseEntity<ResultPaginationDTO<ResBrandDTO>> handleFindAllBrands(@Spec(path = "name", spec = Like.class) Specification<Brand> spec,
                                                                                Pageable pageable) {
        return ResponseEntity.ok().body(brandService.findAllBrands(spec, pageable));
    }

    @GetMapping("/brands/{id}")
    @APIMessage("Get brand by id")
    public ResponseEntity<ResBrandDTO> handleFindBrandById(@PathVariable long id) {
        return ResponseEntity.ok().body(brandService.findById(id));
    }

    @GetMapping("/brands/slugs/{slug}")
    @APIMessage("Get brand by slug")
    public ResponseEntity<ResBrandDTO> handleFindBrandBySlug(@PathVariable String slug) {
        return  ResponseEntity.ok().body(brandService.findBySlug(slug));
    }

    @PutMapping("/brands")
    @APIMessage("Update brand")
    public ResponseEntity<ResBrandDTO> handleUpdateBrand(@RequestBody Brand brand) {
        return ResponseEntity.ok().body(brandService.updateBrand(brand));
    }

    @DeleteMapping("/brands/{id}")
    @APIMessage("Delete a brand")
    public ResponseEntity<Void> handleDeleteBrand(@PathVariable long id) {
        brandService.deleteBrand(id);
        return ResponseEntity.ok().body(null);
    }
}
