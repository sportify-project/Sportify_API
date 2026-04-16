package sport.store.thinh.service;

import com.github.slugify.Slugify;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sport.store.thinh.domain.Brand;
import sport.store.thinh.domain.Users;
import sport.store.thinh.domain.dto.response.ResBrandDTO;
import sport.store.thinh.domain.dto.response.ResUserDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.repository.BrandRepository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

@Service
public class BrandService {
    private final BrandRepository brandRepository;

    private final FileUploadService fileUploadService;

    public BrandService(BrandRepository brandRepository, FileUploadService fileUploadService) {
        this.brandRepository = brandRepository;
        this.fileUploadService = fileUploadService;
    }

    public List<Brand> findAllByOrderByNameAsc() {
        return brandRepository.findAll();
    }

    public ResBrandDTO createBrand(Brand brand, MultipartFile file) {
        if(!brandRepository.existsByName(brand.getName())){
            Slugify slg = Slugify.builder().build();
            brand.setSlug(slg.slugify(brand.getName()));
            if(file != null && !file.isEmpty()){
                try {
                    String fileName = fileUploadService.store(file, "brands");
                    brand.setLogoUrl(fileName);
                } catch (IOException e) {
                    throw new RuntimeException("Lỗi upload file: " + e.getMessage());
                }
            }
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

    public boolean existsByName(String name) {
        return brandRepository.existsByName(name);
    }

    public ResBrandDTO updateBrand(Brand brand, MultipartFile file) {
        Optional<Brand> optionalBrand = brandRepository.findById(brand.getId());
        Slugify slg = Slugify.builder().build();
        brand.setSlug(slg.slugify(brand.getName()));
        if(optionalBrand.isPresent()) {
            Brand brandToUpdate = optionalBrand.get();
            brandToUpdate.setName(brand.getName());
            brandToUpdate.setSlug(brand.getSlug());
            brandToUpdate.setDescription(brand.getDescription());
            brandToUpdate.setCountry(brand.getCountry());
            if(file != null && !file.isEmpty()){
                try {
                    String oldImage = brandToUpdate.getLogoUrl();
                    if(oldImage != null && !oldImage.isEmpty()){
                        fileUploadService.deleteFile(oldImage, "brands");
                    }
                    String fileName = fileUploadService.store(file, "brands");
                    brandToUpdate.setLogoUrl(fileName);
                } catch (IOException e) {
                    throw new RuntimeException("Lỗi upload file: " + e.getMessage());
                }
            }
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
        else throw new NoSuchElementException("Brand not found");
    }

    public ResBrandDTO convertToDTO(Brand brand) {
        ResBrandDTO dto = new ResBrandDTO();
        dto.setId(brand.getId());
        dto.setName(brand.getName());
        dto.setSlug(brand.getSlug());
        dto.setDescription(brand.getDescription());
        dto.setCountry(brand.getCountry());
        if (brand.getLogoUrl() != null && !brand.getLogoUrl().isEmpty()) {
            dto.setImage("/storage/brands/" + brand.getLogoUrl());
        } else {
            dto.setImage(null);
        }
        return dto;
    }

}
