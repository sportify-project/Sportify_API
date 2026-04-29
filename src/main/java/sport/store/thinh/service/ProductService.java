package sport.store.thinh.service;

import com.github.slugify.Slugify;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import sport.store.thinh.domain.*;
import sport.store.thinh.domain.dto.request.ReqProductDTO;
import sport.store.thinh.domain.dto.response.ResBrandDTO;
import sport.store.thinh.domain.dto.response.ResProductDTO;
import sport.store.thinh.domain.dto.response.ResProductVariantDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.repository.*;
import org.springframework.web.multipart.MultipartFile;
import sport.store.thinh.util.constant.ProductStatus;
import sport.store.thinh.util.error.IdInvalidException;
import java.io.IOException;

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
    private final FileUploadService fileUploadService;

    public ProductService(ProductRepository productRepository, ProductImageRepository productImageRepository, ProductVariantRepository productVariantRepository, BrandRepository brandRepository, CategoryRepository categoryRepository, FileUploadService fileUploadService) {
        this.productRepository = productRepository;
        this.productImageRepository = productImageRepository;
        this.productVariantRepository = productVariantRepository;
        this.brandRepository = brandRepository;
        this.categoryRepository = categoryRepository;
        this.fileUploadService = fileUploadService;
    }

    @Transactional
    public ResProductDTO createProduct(ReqProductDTO reqProductDTO, MultipartFile file) {
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

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = fileUploadService.store(file, "products");
                ProductImage image = new ProductImage();
                image.setProduct(savedProduct);
                image.setImageUrl(fileName);
                image.setDisplayOrder(0);
                image.setIsPrimary(true);
                productImageRepository.save(image);
                if (savedProduct.getImages() == null) {
                    savedProduct.setImages(new ArrayList<>());
                }
                savedProduct.getImages().add(image);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload file: " + e.getMessage());
            }
        }

        return convertToResDTO(savedProduct);
    }

    public boolean existsById(Long id) {
        return productRepository.existsById(id);
    }

    @Transactional
    public ResProductDTO editProduct(ReqProductDTO reqProductDTO, MultipartFile file) {
        //Tìm product
        Product productNeedToEdit = productRepository.findById(reqProductDTO.getProductId())
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy sản phẩm với ID: " + reqProductDTO.getProductId()));
        //Cập nhật product
        productNeedToEdit.setName(reqProductDTO.getProductName());
        Brand brandProxy = brandRepository.getReferenceById(reqProductDTO.getBrandId());
        productNeedToEdit.setBrand(brandProxy);

        Category categoryProxy = categoryRepository.getReferenceById(reqProductDTO.getCategoryId());
        productNeedToEdit.setCategory(categoryProxy);
        productNeedToEdit.setDescription(reqProductDTO.getDescription());
        productNeedToEdit.setSku(reqProductDTO.getSku());
        productNeedToEdit.setBasePrice(reqProductDTO.getBasePrice());
        productNeedToEdit.setStockQuantity(reqProductDTO.getStockQuantity());
        productNeedToEdit.setStatus(reqProductDTO.getStatus());

        // Update Variants
        if (reqProductDTO.getVariants() != null) {
            productNeedToEdit.getVariants().clear();
            List<ProductVariant> newVariants = reqProductDTO.getVariants()
                    .stream().map(variant -> convertToVariant(variant, productNeedToEdit)).toList();
            productNeedToEdit.getVariants().addAll(newVariants);
        }

        // Update Images (DTO based)
        if (reqProductDTO.getImages() != null) {
            productNeedToEdit.getImages().clear();
            List<ProductImage> newImages = reqProductDTO.getImages()
                    .stream().map(image -> convertToImage(image, productNeedToEdit)).toList();
            productNeedToEdit.getImages().addAll(newImages);
        }

        if (file != null && !file.isEmpty()) {
            try {
                String fileName = fileUploadService.store(file, "products");
                ProductImage newImage = new ProductImage();
                newImage.setProduct(productNeedToEdit);
                newImage.setImageUrl(fileName);
                newImage.setDisplayOrder(0);
                newImage.setIsPrimary(true);
                productImageRepository.save(newImage);
                if (productNeedToEdit.getImages() == null) {
                    productNeedToEdit.setImages(new ArrayList<>());
                } else {
                    for (ProductImage img : productNeedToEdit.getImages()) {
                        if (img.getIsPrimary() != null && img.getIsPrimary()) {
                            img.setIsPrimary(false);
                            productImageRepository.save(img);
                        }
                    }
                }
                productNeedToEdit.getImages().add(newImage);
            } catch (IOException e) {
                throw new RuntimeException("Lỗi upload file: " + e.getMessage());
            }
        }

        return convertToResDTO(productNeedToEdit);
    }

    public ResultPaginationDTO<ResProductDTO> getAllProducts(Specification<Product> spec, Pageable pageable) {
        Page<Product> productPage;
        if (spec != null) {
            productPage = productRepository.findAll(spec, pageable);
        } else {
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

    public ResProductDTO getProductById(Long id) {
        Product p =  productRepository.findById(id).orElse(null);
        if(p == null){
            throw new NoSuchElementException("Không tồn tại sản phẩm với id là "+ id);
        }
        return convertToResDTO(p);
    }

    public ResultPaginationDTO<ResProductDTO> getProductByCategory(Long categoryId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByCategoryId(categoryId, pageable);
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

    public ResultPaginationDTO<ResProductDTO> getProductByBrand(Long brandId, Pageable pageable) {
        Page<Product> productPage = productRepository.findByBrandId(brandId, pageable);
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

    public ResultPaginationDTO<ResProductVariantDTO> getAllVariants(Specification<ProductVariant> spec, Pageable pageable) {
        Page<ProductVariant> variantPage = (spec != null)
                ? productVariantRepository.findAll(spec, pageable)
                : productVariantRepository.findAll(pageable);
        
        List<ResProductVariantDTO> variantDTOList = variantPage.getContent().stream()
                .map(this::convertToResProductVariantDTO).toList();

        ResultPaginationDTO<ResProductVariantDTO> resultPaginationDTO = new ResultPaginationDTO<>();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(variantPage.getTotalElements());
        meta.setPages(variantPage.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(variantDTOList);
        return resultPaginationDTO;
    }

    private ResProductVariantDTO convertToResProductVariantDTO(ProductVariant variant) {
        ResProductVariantDTO dto = new ResProductVariantDTO();
        dto.setId(variant.getId());
        dto.setSku(variant.getSku());
        dto.setSize(variant.getSize());
        dto.setColor(variant.getColor());
        dto.setPrice(variant.getPrice());
        dto.setStockQuantity(variant.getStockQuantity());

        if (variant.getProduct() != null) {
            ResProductVariantDTO.ProductInnerDTO pDto = new ResProductVariantDTO.ProductInnerDTO();
            pDto.setId(variant.getProduct().getId());
            pDto.setName(variant.getProduct().getName());
            dto.setProduct(pDto);
        }
        return dto;
    }

    //Converter

    private ResProductDTO convertToResDTO(Product product) {
        ResProductDTO resProductDTO = new ResProductDTO();
        resProductDTO.setProductId(product.getId());
        resProductDTO.setProductName(product.getName());
        resProductDTO.setBasePrice(product.getBasePrice());
        resProductDTO.setBrandId(product.getBrand().getId());
        resProductDTO.setBrandName(product.getBrand().getName());
        resProductDTO.setCategoryId(product.getCategory().getId());
        resProductDTO.setCategoryName(product.getCategory().getName());
        resProductDTO.setSku(product.getSku());
        resProductDTO.setSlug(product.getSlug());
        resProductDTO.setDescription(product.getDescription());
        if (product.getImages() != null) {
            resProductDTO.setImages(product.getImages().stream().map(image -> convertFromImageResDTO(image)).toList());
        }
        if (product.getVariants() != null) {
            resProductDTO.setVariants(product.getVariants().stream().map(variant -> convertFromVariantResDTO(variant)).toList());
        }
        return resProductDTO;

    }

    public Product convertFromReqDTO(ReqProductDTO reqProductDTO) {
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
        
        String sku = dto.getSku();
        if (sku == null || sku.trim().isEmpty()) {
            sku = product.getSku() + "-" + dto.getColor() + "-" + dto.getSize();
            sku = sku.replaceAll("\\s+", "").toUpperCase();
        }
        variant.setSku(sku);
        
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
        // prepend storage path if it doesn't have http
        if (productImage.getImageUrl() != null && !productImage.getImageUrl().startsWith("http")) {
            imageDTO.setUrl("/storage/products/" + productImage.getImageUrl());
        } else {
            imageDTO.setUrl(productImage.getImageUrl());
        }
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
