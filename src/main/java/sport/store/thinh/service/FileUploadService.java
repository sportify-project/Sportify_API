package sport.store.thinh.service; // Đổi package nếu cần

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileUploadService {

    // Inject đường dẫn gốc từ file properties
    @Value("${thinh.upload-file.base-uri}")
    private String baseURI;

    /**
     * Hàm lưu file
     * @param file File từ frontend gửi lên
     * @param subFolder Thư mục con (ví dụ: "brands", "products")
     * @return Tên file đã lưu (để lưu vào DB)
     */
    public String store(MultipartFile file, String subFolder) throws IOException {
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path rootLocation = Paths.get(baseURI).resolve(subFolder);
        Files.createDirectories(rootLocation);
        Path destinationFile = rootLocation.resolve(fileName)
                .normalize().toAbsolutePath();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, destinationFile, StandardCopyOption.REPLACE_EXISTING);
        }

        return fileName;
    }

    public void deleteFile(String fileName, String subFolder) {
        try {
            Path root = Paths.get(baseURI).resolve(subFolder);
            Path file = root.resolve(fileName);
            Files.deleteIfExists(file);

            System.out.println("Đã xóa file cũ: " + fileName);
        } catch (IOException e) {
            System.err.println("Không thể xóa file cũ: " + e.getMessage());
        }
    }
}