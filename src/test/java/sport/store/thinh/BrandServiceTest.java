package sport.store.thinh;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import sport.store.thinh.domain.Brand;
import sport.store.thinh.domain.dto.response.ResBrandDTO;
import sport.store.thinh.repository.BrandRepository;
import sport.store.thinh.service.BrandService;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BrandServiceTest {

    @Mock
    private BrandRepository brandRepository;

    @InjectMocks
    private BrandService brandService;

    //Variables
    private Brand inputBrandNew;
    private Brand existingBrand;
    private Brand updatedBrand;

    @BeforeEach
    @DisplayName("Khởi tạo dữ liệu trước mỗi Test Case")
    void setUp() {
        inputBrandNew = new Brand(null, "Uniqlo", null, "Công ty thời trang giá rẻ hàng đầu", "uniqlo.jpg", "Trung quốc");

        existingBrand = new Brand(1L, "Uniqlo", null, "Công ty thời trang giá rẻ hàng đầu", "uniqlo.jpg", "Trung quốc");

        updatedBrand = new Brand(1L, "Uniqlo new", null, "Công ty thời trang giá rẻ hàng đầu", "uniqlo.jpg", "Trung quốc");
    }

    @AfterEach
    @DisplayName("Dọn dẹp sau mỗi Test Case")
    void tearDown() {
        Mockito.reset(brandRepository);
    }

    //Test cases

    @Test
    @DisplayName("Test thêm thương hiệu mới thành công")
    public void createBrand_shouldReturnBrand_whenNameIsValid(TestReporter reporter) {
        // Arrange
        when(brandRepository.existsByName(inputBrandNew.getName())).thenReturn(false);
        when(brandRepository.save(inputBrandNew)).thenReturn(existingBrand);

        // Act
        ResBrandDTO savedBrand = brandService.createBrand(inputBrandNew, null);

        reporter.publishEntry("Expected", "1");
        reporter.publishEntry("Actual", String.valueOf(savedBrand.getId()));

        // Assert
        assertEquals(1, savedBrand.getId());
    }

    @Test
    @DisplayName("Test thêm thương hiệu mới thất bại")
    public void createBrand_shouldReturnException_whenBrandIsExists() {
        // Arrange
        when(brandRepository.existsByName(inputBrandNew.getName())).thenReturn(true);

        // Act & Assert
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () -> {
            brandService.createBrand(inputBrandNew, null);
        });
        assertEquals("Brand already exists", exception.getMessage());
    }

    @Test
    @DisplayName("Test cập nhật thương hiệu mới thành công")
    public void updateBrand_shouldReturnBrand_whenBrandNameIsValid(TestReporter reporter) {
        // Arrange
        when(brandRepository.findById(1L)).thenReturn(Optional.of(existingBrand));
        when(brandRepository.save(any(Brand.class))).thenReturn(updatedBrand);

        // Act
        ResBrandDTO resultBrand = brandService.updateBrand(updatedBrand, null);

        reporter.publishEntry("Expected", "Uniqlo new");
        reporter.publishEntry("Actual", resultBrand.getName());

        // Assert
        assertEquals("Uniqlo new", resultBrand.getName());
    }

    @Test
    @DisplayName("Test cập nhật thương hiệu thất bại do không tồn tại")
    public void updateBrand_shouldReturnException_whenBrandIsNotExists() {
        // Arrange
        when(brandRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            brandService.updateBrand(updatedBrand, null);
        });
        assertEquals("Brand not found", exception.getMessage());
    }

    @Test
    @DisplayName("Test xoá thương hiệu thành công")
    public void deleteBrand_shouldReturnSuccess_whenBrandIsExists() {
        // Arrange
        when(brandRepository.existsById(1L)).thenReturn(true);

        // Act
        brandService.deleteBrand(1L);

        // Assert
        verify(brandRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Test xoá thương hiệu thất bại do không tồn tại")
    public void deleteBrand_shouldReturnFailed_whenBrandIsNotExists() {
        // Arrange
        when(brandRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        NoSuchElementException exception = assertThrows(NoSuchElementException.class, () -> {
            brandService.deleteBrand(1L);
        });
        assertEquals("Brand not found", exception.getMessage());
        verify(brandRepository, never()).deleteById(anyLong());
    }
}
