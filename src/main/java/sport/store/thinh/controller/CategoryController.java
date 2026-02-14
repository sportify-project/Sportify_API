package sport.store.thinh.controller;

import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sport.store.thinh.domain.Brand;
import sport.store.thinh.domain.Category;
import sport.store.thinh.domain.dto.request.ReqCategoryDTO;
import sport.store.thinh.domain.dto.response.ResCategoryDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.service.CategoryService;
import sport.store.thinh.util.annotation.APIMessage;

@RestController
@RequestMapping("/api/v1")
public class CategoryController {
    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/categories")
    @APIMessage("Get all categories")
    public ResponseEntity<ResultPaginationDTO<ResCategoryDTO>> getAllCategories(@Spec(path = "name", spec = Like.class) Specification<Category> spec,
                                                                                Pageable pageable) {
        return ResponseEntity.ok().body(categoryService.getAllCategories(spec, pageable));
    }

    @PostMapping("/categories")
    @APIMessage("Create new category")
    public ResponseEntity<ResCategoryDTO> createCategory(@RequestBody ReqCategoryDTO reqCategoryDTO) {
        return ResponseEntity.status(201).body(categoryService.createNewCategory(reqCategoryDTO));
    }

    @PutMapping("/categories")
    @APIMessage("Edit category")
    public ResponseEntity<ResCategoryDTO> editCategory(@RequestBody ReqCategoryDTO reqCategoryDTO) {
        return ResponseEntity.status(200).body(categoryService.editCategory(reqCategoryDTO));
    }
}
