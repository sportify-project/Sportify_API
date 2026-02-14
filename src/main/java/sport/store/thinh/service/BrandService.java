package sport.store.thinh.service;

import com.github.slugify.Slugify;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sport.store.thinh.domain.Brand;
import sport.store.thinh.domain.Users;
import sport.store.thinh.domain.dto.response.ResBrandDTO;
import sport.store.thinh.domain.dto.response.ResUserDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.repository.BrandRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BrandService {
    private final BrandRepository brandRepository;

    public BrandService(BrandRepository brandRepository) {
        this.brandRepository = brandRepository;
    }

    public List<Brand> findAllByOrderByNameAsc() {
        return brandRepository.findAll();
    }

    public ResBrandDTO createBrand(Brand brand) {
        if(!brandRepository.existsByName(brand.getName())){
            Slugify slg = Slugify.builder().build();
            brand.setSlug(slg.slugify(brand.getName()));
            Brand savedBrand = brandRepository.save(brand);
            return convertToDTO(savedBrand);
        }
        throw new IllegalArgumentException("Brand already exists");
    }

    public ResultPaginationDTO<ResBrandDTO> findAllBrands(Specification<Brand> spec, Pageable pageable) {
        Page<Brand> brandPage;
        if(spec != null)
        {
            brandPage = brandRepository.findAll(spec, pageable);
        }
        else
        {
            brandPage = brandRepository.findAll(pageable);
        }
        List<ResBrandDTO> brandDTOList = brandPage.getContent().stream().map(this::convertToDTO).toList();
        ResultPaginationDTO<ResBrandDTO> resultPaginationDTO = new ResultPaginationDTO<>();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(brandPage.getTotalElements());
        meta.setPages(brandPage.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(brandDTOList);
        return resultPaginationDTO;
    }

    public ResBrandDTO findById(long id) {
        Brand brand = brandRepository.findById(id).orElse(null);
        if(brand != null) {
        return convertToDTO(brand);
        }
        else throw new NoSuchElementException("Brand not found");
    }

    public ResBrandDTO findBySlug(String slug) {
        Brand brand = brandRepository.findBySlug(slug);
        if(brand != null) {
            return convertToDTO(brand);
        }
        else throw new NoSuchElementException("Brand not found");
    }

    public ResBrandDTO updateBrand(Brand brand) {
        Optional<Brand> optionalBrand = brandRepository.findById(brand.getId());
        Slugify slg = Slugify.builder().build();
        brand.setSlug(slg.slugify(brand.getName()));
        if(optionalBrand.isPresent()) {
            Brand brandToUpdate = optionalBrand.get();
            brandToUpdate.setName(brand.getName());
            brandToUpdate.setSlug(brand.getSlug());
            brandToUpdate.setDescription(brand.getDescription());
            brandToUpdate.setCountry(brand.getCountry());
            brandToUpdate.setLogoUrl(brand.getLogoUrl());
            brandRepository.save(brandToUpdate);
            return convertToDTO(brand);
        }else  {
            throw new NoSuchElementException("Brand not found");
        }
    }

    public void deleteBrand(long id) {
        if(brandRepository.existsById(id)) {
            brandRepository.deleteById(id);
        }
        else throw new NoSuchElementException("Không tồn tại brand với id này");
    }

    public ResBrandDTO convertToDTO(Brand brand) {
        ResBrandDTO dto = new ResBrandDTO();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setSlug(brand.getSlug());
        dto.setDescription(brand.getDescription());
        dto.setImage(brand.getLogoUrl());
        dto.setCountry(brand.getCountry());
        return dto;
    }

}
