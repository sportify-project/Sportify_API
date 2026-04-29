package sport.store.thinh.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sport.store.thinh.domain.Supplier;
import sport.store.thinh.domain.dto.request.ReqSupplierDTO;
import sport.store.thinh.domain.dto.response.ResSupplierDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.repository.SupplierRepository;

@Service
public class SupplierService {

    private final SupplierRepository supplierRepository;

    public SupplierService(SupplierRepository supplierRepository) {
        this.supplierRepository = supplierRepository;
    }

    // ─── CRUD ────────────────────────────────────────────────────────────────

    @Transactional
    public ResSupplierDTO create(ReqSupplierDTO dto) {
        Supplier supplier = new Supplier();
        mapFromDto(dto, supplier);
        supplierRepository.save(supplier);
        return toDto(supplier);
    }

    @Transactional
    public ResSupplierDTO update(Long id, ReqSupplierDTO dto) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));
        mapFromDto(dto, supplier);
        supplierRepository.save(supplier);
        return toDto(supplier);
    }

    public ResSupplierDTO findById(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));
        return toDto(supplier);
    }

    public ResultPaginationDTO<ResSupplierDTO> findAll(Specification<Supplier> spec, Pageable pageable) {
        Page<Supplier> pageSupplier = (spec == null)
                ? supplierRepository.findAll(pageable)
                : supplierRepository.findAll(spec, pageable);

        Page<ResSupplierDTO> page = pageSupplier.map(this::toDto);
        ResultPaginationDTO<ResSupplierDTO> result = new ResultPaginationDTO<>();
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
    public void deactivate(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Supplier not found: " + id));
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void mapFromDto(ReqSupplierDTO dto, Supplier supplier) {
        supplier.setName(dto.getName());
        supplier.setPhone(dto.getPhone());
        supplier.setEmail(dto.getEmail());
        supplier.setAddress(dto.getAddress());
        supplier.setTaxCode(dto.getTaxCode());
    }

    public ResSupplierDTO toDto(Supplier supplier) {
        ResSupplierDTO dto = new ResSupplierDTO();
        dto.setId(supplier.getId());
        dto.setName(supplier.getName());
        dto.setPhone(supplier.getPhone());
        dto.setEmail(supplier.getEmail());
        dto.setAddress(supplier.getAddress());
        dto.setTaxCode(supplier.getTaxCode());
        dto.setActive(supplier.isActive());
        dto.setCreatedAt(supplier.getCreatedAt());
        dto.setCreatedBy(supplier.getCreatedBy());
        return dto;
    }
}
