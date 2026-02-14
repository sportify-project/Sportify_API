package sport.store.thinh.service;

import com.github.slugify.Slugify;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sport.store.thinh.domain.Product;
import sport.store.thinh.domain.ProductImage;
import sport.store.thinh.domain.ProductVariant;
import sport.store.thinh.domain.dto.request.ReqProductDTO;
import sport.store.thinh.domain.dto.response.ResBrandDTO;
import sport.store.thinh.domain.dto.response.ResProductDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.repository.*;
import sport.store.thinh.util.constant.ProductStatus;
import sport.store.thinh.util.error.IdInvalidException;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

@Repository
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductImageRepository productImageRepository;
    private final ProductVariantRepository productVariantRepository;
    private final BrandRepository brandRepository;
    private final CategoryRepository categoryRepository;

    public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository, ProductVariantRepository productVariantRepository, BrandRepository brandRepository, CategoryRepository categoryRepository) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
    }

    @Transactional
    public ResProductDTO createProduct(ReqProductDTO reqProductDTO){
        Product savedProduct = productRepository.save(convertFromReqDTO(reqProductDTO));
        if (reqProductDTO.getVariants() != null && !reqProductDTO.getVariants().isEmpty()) {
            List<ProductVariant> variants = reqProductDTO.getVariants()
                    .stream().map(variant -> convertToVariant(variant, savedProduct)).toList();
            productVariantRepository.saveAll(variants);
            savedProduct.setVariants(variants);
        }

        if (reqProductDTO.getImages() != null && !reqProductDTO.getImages().isEmpty()) {
            List<ProductImage> images = reqProductDTO.getImages()
                    .stream().map(image -> convertToImage(image, savedProduct)).toList();
            productImageRepository.saveAll(images);
            savedProduct.setImages(images);
        }

        return convertToResDTO(savedProduct);
    }

    public ResultPaginationDTO<ResProductDTO> getAllProducts(Specification<Product> spec, Pageable pageable){
        Page<Product> productPage;
        if(spec != null)
        {
            productPage = productRepository.findAll(spec, pageable);
        }
        else
        {
            productPage = productRepository.findAll(pageable);
        }
        List<ResProductDTO> productDTOList = productPage.getContent().stream().map(this::convertToResDTO).toList();
        ResultPaginationDTO<ResProductDTO> resultPaginationDTO = new ResultPaginationDTO<>();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(productPage.getTotalElements());
        meta.setPages(productPage.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(productDTOList);
        return resultPaginationDTO;
    }

    private ResProductDTO convertToResDTO(Product product) {
        ResProductDTO resProductDTO = new ResProductDTO();
        resProductDTO.setProductId(product.getId());
        resProductDTO.setProductName(product.getName());
        resProductDTO.setBrandId(product.getBrand().getId());
        resProductDTO.setBrandName(product.getBrand().getName());
        resProductDTO.setCategoryId(product.getCategory().getId());
        resProductDTO.setCategoryName(product.getCategory().getName());
        resProductDTO.setSku(product.getSku());
        resProductDTO.setSlug(product.getSlug());
        resProductDTO.setDescription(product.getDescription());
        if(product.getImages() != null) {
            resProductDTO.setImages(product.getImages().stream().map(image -> convertFromImageResDTO(image)).toList());
        }
        if(product.getVariants() != null) {
        resProductDTO.setVariants(product.getVariants().stream().map(variant -> convertFromVariantResDTO(variant)).toList());
        }
        return resProductDTO;

    }

    public Product convertFromReqDTO(ReqProductDTO reqProductDTO){
        Product product = new Product();
        Slugify slg = Slugify.builder().build();
        product.setName(reqProductDTO.getProductName());
        product.setSlug(slg.slugify(reqProductDTO.getProductName()));
        product.setDescription(reqProductDTO.getDescription());
        product.setSku(reqProductDTO.getSku());
        product.setCategory(categoryRepository
                .findById(reqProductDTO.getCategoryId())
                .orElseThrow(() -> new NoSuchElementException("Category Not Found")));
        product.setBrand(brandRepository
                .findById(reqProductDTO.getBrandId())
                .orElseThrow(() -> new NoSuchElementException("Brand Not Found")));
        product.setBasePrice(reqProductDTO.getBasePrice());
        product.setStockQuantity(reqProductDTO.getStockQuantity());
        product.setStatus(ProductStatus.ACTIVE.value());

        return product;
    }

    private ProductVariant convertToVariant(ReqProductDTO.VariantDTO dto, Product product) {
        ProductVariant variant = new ProductVariant();
        variant.setProduct(product);
        variant.setSku(dto.getSku());
        variant.setColor(dto.getColor());
        variant.setSize(dto.getSize());
        variant.setPrice(dto.getPrice());
        variant.setStockQuantity(dto.getStockQuantity());
        return variant;
    }

    private ProductImage convertToImage(ReqProductDTO.ImageDTO dto, Product product) {
        ProductImage image = new ProductImage();
        image.setProduct(product);
        image.setImageUrl(dto.getUrl());
        image.setDisplayOrder(dto.getDisplayOrder());
        image.setIsPrimary(dto.getIsPrimary());
        return image;
    }

    public ResProductDTO.ImageDTO convertFromImageResDTO(ProductImage productImage) {
        ResProductDTO.ImageDTO imageDTO = new ResProductDTO.ImageDTO();
        imageDTO.setImageId(productImage.getId());
        imageDTO.setUrl(productImage.getImageUrl());
        imageDTO.setDisplayOrder(productImage.getDisplayOrder());
        return imageDTO;
    }

    public ResProductDTO.VariantDTO convertFromVariantResDTO(ProductVariant productVariant) {
        ResProductDTO.VariantDTO variantDTO = new ResProductDTO.VariantDTO();
        variantDTO.setVariantId(productVariant.getId());
        variantDTO.setSize(productVariant.getSize());
        variantDTO.setStockQuantity(productVariant.getStockQuantity());
        variantDTO.setPrice(productVariant.getPrice());
        variantDTO.setColor(productVariant.getColor());
        return variantDTO;
    }
}
