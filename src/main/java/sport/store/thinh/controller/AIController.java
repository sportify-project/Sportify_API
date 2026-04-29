package sport.store.thinh.controller;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sport.store.thinh.domain.dto.request.ReqGoodsReceiptDTO;
import sport.store.thinh.service.AIService;
import sport.store.thinh.util.annotation.APIMessage;

@RestController
@RequestMapping("/api/v1/ai")
public class AIController {

    private final AIService aiService;

    public AIController(AIService aiService) {
        this.aiService = aiService;
    }

    @PostMapping("/scan-invoice")
    @APIMessage("Scan invoice using AI")
    public ResponseEntity<ReqGoodsReceiptDTO> scanInvoice(@RequestParam("file") MultipartFile file) throws Exception {
        String text = aiService.extractTextFromPdf(file);
        JsonNode rawJson = aiService.analyzeInvoice(text);
        ReqGoodsReceiptDTO result = aiService.mapToReceiptDTO(rawJson);
        return ResponseEntity.ok(result);
    }
}
