package sport.store.thinh;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sport.store.thinh.domain.Brand;
import sport.store.thinh.domain.dto.response.ResBrandDTO;
import sport.store.thinh.repository.BrandRepository;
import sport.store.thinh.service.BrandService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    @Test
    public void createBrand_shouldReturnBrand_whenNameIsValid() {
        //arange
        Brand inputBrand = new Brand("Uniqlo", null, "Công ty thời trang giá rẻ hàng đầu", "uniqlo.jpg", "Trung quốc", null, null, null, null, null);

        Brand outputBrand = new Brand(1, "Uniqlo", null, "Công ty thời trang giá rẻ hàng đầu", "uniqlo.jpg", "Trung quốc", null, null, null, null, null);

        when(this.brandRepository.existsByName(inputBrand.getName())).thenReturn(false);

        when(this.brandRepository.save(inputBrand)).thenReturn(outputBrand);

        //act
        ResBrandDTO savedBrand = brandService.createBrand(inputBrand);
        //assert
        assertEquals(1, savedBrand.getId());
    }

    @Test
    public void createBrand_shouldReturnException_whenNameIsNotValid() {
        //arange
        Brand inputBrand = new Brand("Uniqlo", null, "Công ty thời trang giá rẻ hàng đầu", "uniqlo.jpg", "Trung quốc", null, null, null, null, null);

        when(this.brandRepository.existsByName(inputBrand.getName())).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            brandService.createBrand(inputBrand);
        });

        //act
        assertEquals("Brand already exists", exception.getMessage());
    }
}
