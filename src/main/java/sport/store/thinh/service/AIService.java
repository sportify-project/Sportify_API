package sport.store.thinh.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class AIService {

    @Value("${thinh.groq.api-key}")
    private String groqApiKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final sport.store.thinh.repository.SupplierRepository supplierRepository;
    private final sport.store.thinh.repository.ProductVariantRepository productVariantRepository;

    private final String GROQ_URL = "https://api.groq.com/openai/v1/chat/completions";

    public AIService(RestTemplate restTemplate, 
                      ObjectMapper objectMapper,
                      sport.store.thinh.repository.SupplierRepository supplierRepository,
                      sport.store.thinh.repository.ProductVariantRepository productVariantRepository) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
        this.supplierRepository = supplierRepository;
        this.productVariantRepository = productVariantRepository;
    }

    public sport.store.thinh.domain.dto.request.ReqGoodsReceiptDTO mapToReceiptDTO(JsonNode rawJson) {
        sport.store.thinh.domain.dto.request.ReqGoodsReceiptDTO dto = new sport.store.thinh.domain.dto.request.ReqGoodsReceiptDTO();
        
        // Find Supplier
        String supplierName = rawJson.path("supplierName").asText();
        supplierRepository.findFirstByNameContainingIgnoreCase(supplierName)
                .ifPresent(s -> dto.setSupplierId(s.getId()));

        dto.setNotes(rawJson.path("notes").asText());
        
        List<sport.store.thinh.domain.dto.request.ReqGoodsReceiptDTO.ReqGoodsReceiptItemDTO> items = new ArrayList<>();
        JsonNode rawItems = rawJson.path("items");
        if (rawItems.isArray()) {
            for (JsonNode node : rawItems) {
                sport.store.thinh.domain.dto.request.ReqGoodsReceiptDTO.ReqGoodsReceiptItemDTO itemDto = 
                        new sport.store.thinh.domain.dto.request.ReqGoodsReceiptDTO.ReqGoodsReceiptItemDTO();
                
                String productName = node.path("productName").asText();
                String sku = node.path("sku").asText();
                
                // Try to find variant by SKU first, then by name
                productVariantRepository.findFirstBySku(sku)
                        .or(() -> productVariantRepository.findFirstByProduct_NameContainingIgnoreCase(productName))
                        .ifPresent(v -> itemDto.setVariantId(v.getId()));
                
                itemDto.setQuantity(node.path("quantity").asInt());
                itemDto.setCostPrice(new java.math.BigDecimal(node.path("costPrice").asText("0")));
                items.add(itemDto);
            }
        }
        dto.setItems(items);
        return dto;
    }

    public String extractTextFromPdf(MultipartFile file) throws IOException {
        try (PDDocument document = Loader.loadPDF(file.getBytes())) {
            PDFTextStripper stripper = new PDFTextStripper();
            return stripper.getText(document);
        }
    }

    public JsonNode analyzeInvoice(String text) throws Exception {
        String prompt = "You are an invoice parsing expert. Extract data from the following invoice text and return ONLY a JSON object. " +
                "Do not include any conversational text. If a field is missing, use null or 0. " +
                "The JSON structure must be: " +
                "{ \"supplierName\": \"string\", \"notes\": \"string\", \"items\": [ " +
                "{ \"productName\": \"string\", \"sku\": \"string\", \"quantity\": integer, \"costPrice\": number } ] } " +
                "\n\nInvoice Text:\n" + text;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(groqApiKey);

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", "llama-3.3-70b-versatile");
        
        List<Map<String, String>> messages = new ArrayList<>();
        messages.add(Map.of("role", "user", "content", prompt));
        requestBody.put("messages", messages);
        requestBody.put("response_format", Map.of("type", "json_object"));

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);
        
        ResponseEntity<String> response = restTemplate.postForEntity(GROQ_URL, entity, String.class);
        
        if (response.getStatusCode() == HttpStatus.OK) {
            JsonNode root = objectMapper.readTree(response.getBody());
            String content = root.path("choices").get(0).path("message").path("content").asText();
            return objectMapper.readTree(content);
        } else {
            throw new RuntimeException("Failed to call Groq API: " + response.getStatusCode());
        }
    }
}
