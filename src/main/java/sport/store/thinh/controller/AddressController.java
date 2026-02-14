package sport.store.thinh.controller;

import jakarta.validation.Valid;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.domain.Like;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sport.store.thinh.domain.Address;
import sport.store.thinh.domain.Brand;
import sport.store.thinh.domain.dto.request.ReqAddressDTO;
import sport.store.thinh.domain.dto.response.ResAddressDTO;
import sport.store.thinh.domain.dto.response.ResBrandDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.service.AddressService;
import sport.store.thinh.util.annotation.APIMessage;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class AddressController {
    private final AddressService addressService;

    public AddressController(AddressService addressService) {
        this.addressService = addressService;
    }
    @GetMapping("/address")
    @APIMessage("Get all addresses")
    public ResponseEntity<ResultPaginationDTO<ResAddressDTO>> handleFindAllAddress(@Spec(path = "phone", spec = Equal.class) Specification<Address> spec,
                                                                                Pageable pageable) {
        return ResponseEntity.ok().body(addressService.findAllAddresses(spec, pageable));
    }

    @PostMapping("/address")
    @APIMessage("Create new address")
    public ResponseEntity<ResAddressDTO> createAddress(@Valid @RequestBody ReqAddressDTO address) {
        return ResponseEntity.status(201).body(addressService.createNewAddress(address));
    }

    @GetMapping("/address/{id}")
    @APIMessage("Get address by user id")
    public ResponseEntity<List<ResAddressDTO>> getAddress(@PathVariable Long id) {
        return ResponseEntity.ok().body(addressService.getAddressByUserId(id));
    }
}
