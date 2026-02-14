package sport.store.thinh.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import sport.store.thinh.domain.Address;
import sport.store.thinh.domain.Brand;
import sport.store.thinh.domain.Users;
import sport.store.thinh.domain.dto.request.ReqAddressDTO;
import sport.store.thinh.domain.dto.response.ResAddressDTO;
import sport.store.thinh.domain.dto.response.ResBrandDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.repository.AddressRepository;
import sport.store.thinh.repository.UserRepository;
import sport.store.thinh.util.SecurityUtil;

import java.util.ArrayList;
import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserRepository userRepository;

    public AddressService(AddressRepository addressRepository, UserRepository userRepository) {
        this.addressRepository = addressRepository;
        this.userRepository = userRepository;
    }

    public ResAddressDTO createNewAddress(ReqAddressDTO addressDTO) {
        Users u = userRepository.findById(addressDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có ID: " + addressDTO.getUserId()));
        if (addressDTO.isDefaultAddress()) {
            addressRepository.unsetDefaultAddresses(u.getUserId());
        }
        return convertToDTO(addressRepository.save(convertFromDTO(addressDTO)));
    }

    public ResultPaginationDTO<ResAddressDTO> findAllAddresses(Specification<Address> spec, Pageable pageable){
        Page<Address> addressPage;
        if(spec != null)
        {
            addressPage = addressRepository.findAll(spec, pageable);
        }
        else
        {
            addressPage = addressRepository.findAll(pageable);
        }
        List<ResAddressDTO> addressDTOList = addressPage.getContent().stream().map(this::convertToDTO).toList();
        ResultPaginationDTO<ResAddressDTO> resultPaginationDTO = new ResultPaginationDTO<>();
        ResultPaginationDTO.Meta meta = new ResultPaginationDTO.Meta();
        meta.setPage(pageable.getPageNumber() + 1);
        meta.setPageSize(pageable.getPageSize());
        meta.setTotal(addressPage.getTotalElements());
        meta.setPages(addressPage.getTotalPages());
        resultPaginationDTO.setMeta(meta);
        resultPaginationDTO.setResult(addressDTOList);
        return resultPaginationDTO;
    }

    public Address convertFromDTO(ReqAddressDTO dto) {
        Address address = new Address();
        address.setFullName(dto.getFullName());
        address.setPhone(dto.getPhone());
        address.setCity(dto.getCity());
        address.setWard(dto.getWard());
        address.setAddressLine1(dto.getAddressLine1());
        address.setAddressLine2(dto.getAddressLine2());
        address.setDefaultAddress(dto.isDefaultAddress());
        address.setUser(userRepository.findById(dto.getUserId()).get());
        return address;
    }

    public ResAddressDTO convertToDTO(Address address) {
        ResAddressDTO dto = new ResAddressDTO();
        dto.setId(address.getId());
        dto.setFullName(address.getFullName());
        dto.setPhone(address.getPhone());
        dto.setCity(address.getCity());
        dto.setWard(address.getWard());
        dto.setAddressLine1(address.getAddressLine1());
        dto.setAddressLine2(address.getAddressLine2());
        dto.setDefaultAddress(address.isDefaultAddress());
        dto.setUserId(address.getUser().getUserId());
        return dto;
    }

    public List<ResAddressDTO> getAddressByUserId(long userId) {
        Users u  = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy người dùng có ID: " + userId));
        List<Address> addresses = u.getAddresses();
        List<ResAddressDTO> resAddressDTOList = addresses.stream().map(this::convertToDTO).toList();
        return resAddressDTOList;
    }
}
