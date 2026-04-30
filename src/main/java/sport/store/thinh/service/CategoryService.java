package sport.store.thinh.service;

import com.github.slugify.Slugify;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sport.store.thinh.domain.Address;
import sport.store.thinh.domain.Category;
import sport.store.thinh.domain.dto.request.ReqAddressDTO;
import sport.store.thinh.domain.dto.request.ReqCategoryDTO;
import sport.store.thinh.domain.dto.response.ResAddressDTO;
import sport.store.thinh.domain.dto.response.ResCategoryDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.repository.CategoryRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class CategoryService {
    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    public ResultPaginationDTO<ResCategoryDTO> getAllCategories(Specification<Category> spec, Pageable pageable) {
        Page<Category> categoryPage;
        if (spec != null) {
            categoryPage = categoryRepository.findAll(spec, pageable);
        } else {
            categoryPage = categoryRepository.findAll(pageable);
        }
        List<ResCategoryDTO> categoryDTOList = categoryPage.getContent().stream().map(this::convertToDTO).toList();
        ResultPaginationDTO<ResCategoryDTO> resultPaginationDTO = new ResultPaginationDTO<>();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(categoryPage.getTotalElements());
        meta.setPages(categoryPage.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(categoryDTOList);
        return resultPaginationDTO;
    }

    public ResCategoryDTO createNewCategory(ReqCategoryDTO dto) {
        Category category = new Category();
        Slugify slg = Slugify.builder().build();
        category.setName(dto.getName());
        category.setSlug(slg.slugify(dto.getName()));
        category.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        category.setDescription(dto.getDescription());
        if (dto.getParentId() != null) {
            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha!"));
            category.setParent(parent);
        }
        categoryRepository.save(category);
        return convertToDTO(category);
    }

    public ResCategoryDTO editCategory(ReqCategoryDTO dto) {
        Category category = categoryRepository.findById(dto.getId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục để cập nhật!"));
        Slugify slg = Slugify.builder().build();
        category.setName(dto.getName());
        category.setSlug(slg.slugify(dto.getName()));
        category.setDisplayOrder(dto.getDisplayOrder() != null ? dto.getDisplayOrder() : 0);
        category.setDescription(dto.getDescription());
        if (dto.getParentId() != null) {
            if (dto.getId().equals(dto.getParentId())) {
                throw new RuntimeException("Một danh mục không thể là cha của chính nó!");
            }

            Category parent = categoryRepository.findById(dto.getParentId())
                    .orElseThrow(() -> new RuntimeException("Không tìm thấy danh mục cha!"));
            category.setParent(parent);
        } else {
            category.setParent(null);
        }
        categoryRepository.save(category);
        return convertToDTO(category);
    }

    public ResCategoryDTO convertToDTO(Category category) {
        ResCategoryDTO resCategoryDTO = new ResCategoryDTO();
        resCategoryDTO.setId(category.getId());
        resCategoryDTO.setName(category.getName());
        resCategoryDTO.setDisplayOrder(category.getDisplayOrder());
        resCategoryDTO.setSlug(category.getSlug());
        if (category.getParent() != null) {
            resCategoryDTO.setParentId(category.getParent().getId());
            resCategoryDTO.setParentName(category.getParent().getName());
        }
        if (category.getChildren() != null && !category.getChildren().isEmpty()) {
            List<ResCategoryDTO> childrenDTO = category.getChildren().stream()
                    .map(this::convertToDTO)
                    .toList();
            resCategoryDTO.setChildren(childrenDTO);
        }
        return resCategoryDTO;
    }
}
