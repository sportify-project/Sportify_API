package sport.store.thinh;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.autoconfigure.SecurityAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.HttpMethod;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import sport.store.thinh.configuration.SecurityConfig;
import sport.store.thinh.controller.BrandController;
import sport.store.thinh.domain.Brand;
import sport.store.thinh.domain.dto.response.ResBrandDTO;
import sport.store.thinh.service.BrandService;

import java.util.NoSuchElementException;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.mock.http.server.reactive.MockServerHttpRequest.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@WebMvcTest(value = BrandController.class)
@AutoConfigureMockMvc(addFilters = false)
public class BrandControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BrandService brandService;

    private ResBrandDTO mockDto;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        mockDto = new ResBrandDTO();
        mockDto.setId(1L);
        mockDto.setName("Uniqlo");
        mockDto.setDescription("Công ty thời trang giá rẻ hàng đầu");
        mockDto.setImage("uniqlo.jpg");
        mockDto.setCountry("Trung quốc");

        mockFile = new MockMultipartFile(
                "file",
                "uniqlo.jpg",
                MediaType.IMAGE_JPEG.toString(),
                "dummy-image-data".getBytes()
        );
    }

    @AfterEach
    void tearDown() {
        Mockito.reset(brandService);
    }

    @Test
    @DisplayName("Test API lấy thông tin Brand trả về HTTP 200")
    void getBrand_shouldReturn200_whenBrandExists() throws Exception {
        // Arrange
        when(brandService.findById(1L)).thenReturn(mockDto);

        // Act & Assert
        mockMvc.perform(get("/api/v1/brands/1")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value("Uniqlo"));
    }

    @Test
    @DisplayName("Test API lấy thông tin Brand trả về error")
    void getBrand_shouldReturnError_whenBrandDoesntExists() throws Exception {
        // Arrange
        when(brandService.findById(1L)).thenThrow(new NoSuchElementException("Brand not found"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/brands/1")
                        .contentType(String.valueOf(MediaType.APPLICATION_JSON)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Brand not found"));
    }

    @Test
    @DisplayName("Test API tạo Brand mới trả về HTTP 201 (Created)")
    void createBrand_shouldReturn201_whenBrandCreatedSuccessfully() throws Exception {

        // ARRANGE
        when(brandService.createBrand(any(Brand.class), any())).thenReturn(mockDto);

        // ACT & ASSERT
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/brands")
                        .file(mockFile)

                        .param("name", "Uniqlo")
                        .param("description", "Công ty thời trang giá rẻ hàng đầu")
                        .param("country", "Trung quốc")
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.name").value("Uniqlo"));
    }

    @Test
    @DisplayName("Test API tạo Brand mới trả về HTTP 400 (Bad Request)")
    void createBrand_shouldReturnException_whenBrandExisted() throws Exception {

        // ARRANGE
        when(brandService.createBrand(any(Brand.class), any())).thenThrow(new IllegalArgumentException("Brand already exists"));

        // ACT & ASSERT
        mockMvc.perform(multipart(HttpMethod.POST, "/api/v1/brands")
                        .file(mockFile)

                        .param("name", "Uniqlo")
                        .param("description", "Công ty thời trang giá rẻ hàng đầu")
                        .param("country", "Trung quốc")
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test API cập nhật Brand mới trả về HTTP 200 (OKE)")
    void updateBrand_shouldReturn200_whenBrandUpdatedSuccessfully() throws Exception {

        // ARRANGE
        ResBrandDTO mockResDtoUpdate = new ResBrandDTO();
        mockResDtoUpdate.setId(1L);
        mockResDtoUpdate.setName("Uniqlo1");
        mockResDtoUpdate.setDescription("Công ty thời trang giá rẻ hàng đầu");
        mockResDtoUpdate.setImage("uniqlo.jpg");
        mockResDtoUpdate.setCountry("Trung quốc");

        when(brandService.updateBrand(any(Brand.class), any())).thenReturn(mockResDtoUpdate);

        // ACT & ASSERT
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/brands")
                        .file(mockFile)

                        .param("name", mockResDtoUpdate.getName())
                        .param("description", mockResDtoUpdate.getDescription())
                        .param("country", mockResDtoUpdate.getCountry())
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.name").value(mockResDtoUpdate.getName()));
    }

    @Test
    @DisplayName("Test API cập nhật Brand mới trả về error")
    void updateBrand_shouldReturnException_whenBrandIsNotExists() throws Exception {

        // ARRANGE
        ResBrandDTO mockResDtoUpdate = new ResBrandDTO();
        mockResDtoUpdate.setId(1L);
        mockResDtoUpdate.setName("Uniqlo1");
        mockResDtoUpdate.setDescription("Công ty thời trang giá rẻ hàng đầu");
        mockResDtoUpdate.setImage("uniqlo.jpg");
        mockResDtoUpdate.setCountry("Trung quốc");

        when(brandService.updateBrand(any(Brand.class), any())).thenThrow(new NoSuchElementException("Brand not found"));

        // ACT & ASSERT
        mockMvc.perform(multipart(HttpMethod.PUT, "/api/v1/brands")
                        .file(mockFile)

                        .param("name", mockResDtoUpdate.getName())
                        .param("description", mockResDtoUpdate.getDescription())
                        .param("country", mockResDtoUpdate.getCountry())
                )
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("Test API xoá Brand thành công")
    void deleteBrand_shouldReturnSuccess_whenBrandIsExists() throws Exception {

        // ARRANGE
        Brand brandNeedToDelete = new Brand();
        brandNeedToDelete.setId(1L);
        brandNeedToDelete.setName("Uniqlo1");
        brandNeedToDelete.setDescription("Công ty thời trang giá rẻ hàng đầu");
        brandNeedToDelete.setLogoUrl("uniqlo.jpg");
        brandNeedToDelete.setCountry("Trung quốc");

        brandService.deleteBrand(brandNeedToDelete.getId());

        // ACT & ASSERT
        mockMvc.perform(multipart(HttpMethod.DELETE, "/api/v1/brands/" + brandNeedToDelete.getId())
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Test API xoá Brand thất bại")
    void deleteBrand_shouldReturnException_whenBrandIsNotExists() throws Exception {

        // ARRANGE
        Brand brandNeedToDelete = new Brand();
        brandNeedToDelete.setId(1L);
        brandNeedToDelete.setName("Uniqlo1");
        brandNeedToDelete.setDescription("Công ty thời trang giá rẻ hàng đầu");
        brandNeedToDelete.setLogoUrl("uniqlo.jpg");
        brandNeedToDelete.setCountry("Trung quốc");

        doThrow(new NoSuchElementException("Brand not found")).when(brandService).deleteBrand(brandNeedToDelete.getId());

        // ACT & ASSERT
        mockMvc.perform(multipart(HttpMethod.DELETE, "/api/v1/brands/" + brandNeedToDelete.getId())
                )
                .andExpect(status().isBadRequest());
    }
}
