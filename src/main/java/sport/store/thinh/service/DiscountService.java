package sport.store.thinh.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sport.store.thinh.domain.Discount;
import sport.store.thinh.domain.ProductVariant;
import sport.store.thinh.domain.dto.request.ReqDiscountDTO;
import sport.store.thinh.domain.dto.response.ResDiscountDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.repository.DiscountRepository;
import sport.store.thinh.repository.ProductVariantRepository;

@Service
public class DiscountService {

    private final DiscountRepository discountRepository;
    private final ProductVariantRepository productVariantRepository;

    public DiscountService(DiscountRepository discountRepository,
                           ProductVariantRepository productVariantRepository) {
        this.discountRepository = discountRepository;
        this.productVariantRepository = productVariantRepository;
    }

    @Transactional
    public ResDiscountDTO create(ReqDiscountDTO dto) {
        Discount discount = new Discount();
        mapToEntity(dto, discount);
        discountRepository.save(discount);
        return toDto(discount);
    }

    @Transactional
    public ResDiscountDTO update(Long id, ReqDiscountDTO dto) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + id));
        mapToEntity(dto, discount);
        discountRepository.save(discount);
        return toDto(discount);
    }

    public ResDiscountDTO findById(Long id) {
        Discount discount = discountRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Discount not found: " + id));
        return toDto(discount);
    }

    public ResultPaginationDTO<ResDiscountDTO> findAll(Specification<Discount> spec, Pageable pageable) {
        Page<Discount> pageDiscount = (spec == null)
                ? discountRepository.findAll(pageable)
                : discountRepository.findAll(spec, pageable);
        
        Page<ResDiscountDTO> page = pageDiscount.map(this::toDto);
        
        ResultPaginationDTO<ResDiscountDTO> result = new ResultPaginationDTO<>();
        result.setResult(page.getContent());
        
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setPages(page.getTotalPages());
        meta.setTotal(page.getTotalElements());
        result.setMeta(meta);
        
        return result;
    }

    @Transactional
    public void delete(Long id) {
        discountRepository.deleteById(id);
    }

    private void mapToEntity(ReqDiscountDTO dto, Discount entity) {
        entity.setDiscountPercentage(dto.getDiscountPercentage());
        entity.setStartDate(dto.getStartDate());
        entity.setEndDate(dto.getEndDate());
        entity.setQuantity(dto.getQuantity());

        if (dto.getVariantId() != null) {
            ProductVariant variant = productVariantRepository.findById(dto.getVariantId())
                    .orElseThrow(() -> new RuntimeException("Variant not found: " + dto.getVariantId()));
            entity.setVariant(variant);
        }
    }

    private ResDiscountDTO toDto(Discount entity) {
        ResDiscountDTO dto = new ResDiscountDTO();
        dto.setDiscountId(entity.getDiscountId());
        dto.setDiscountPercentage(entity.getDiscountPercentage());
        dto.setStartDate(entity.getStartDate());
        dto.setEndDate(entity.getEndDate());
        dto.setQuantity(entity.getQuantity());

        if (entity.getVariant() != null) {
            dto.setVariantId(entity.getVariant().getId());
            dto.setVariantSku(entity.getVariant().getSku());
            dto.setProductName(entity.getVariant().getProduct().getName());
        }
        return dto;
    }
}
