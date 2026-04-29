package sport.store.thinh.controller;

import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sport.store.thinh.domain.Order;
import sport.store.thinh.domain.dto.request.ReqOrderDTO;
import sport.store.thinh.domain.dto.response.ResOrderDTO;
import sport.store.thinh.domain.dto.response.ResultPaginationDTO;
import sport.store.thinh.service.OrderService;
import sport.store.thinh.util.annotation.APIMessage;

import java.util.Map;

@RestController
@RequestMapping("/api/v1")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/orders")
    @APIMessage("Place a new order")
    public ResponseEntity<ResOrderDTO> handlePlaceOrder(@RequestBody ReqOrderDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(orderService.processOrder(dto));
    }

    @GetMapping("/orders")
    @APIMessage("Get all orders")
    public ResponseEntity<ResultPaginationDTO<ResOrderDTO>> handleFindAll(
            @Spec(path = "orderStatus", spec = Equal.class) Specification<Order> spec,
            Pageable pageable) {
        return ResponseEntity.ok(orderService.findAll(spec, pageable));
    }

    @GetMapping("/orders/{id}")
    @APIMessage("Get order details")
    public ResponseEntity<ResOrderDTO> handleFindById(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.fetchById(id));
    }

    @PutMapping("/orders/{id}/status")
    @APIMessage("Update order status")
    public ResponseEntity<ResOrderDTO> handleUpdateStatus(@PathVariable Long id,
                                                          @RequestBody Map<String, String> body) {
        String status = body.get("status");
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }
}
