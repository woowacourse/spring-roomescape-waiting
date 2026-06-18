package roomescape.controller.client.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.facade.ReservationOrderFacade;
import roomescape.application.service.OrderService;
import roomescape.controller.client.api.dto.request.OrderRequest;
import roomescape.controller.client.api.dto.response.OrderResponse;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderApiController {

    private final ReservationOrderFacade reservationOrderService;
    private final OrderService orderService;

    @PostMapping("/reservation")
    public ResponseEntity<OrderResponse> createReservationOrder(@Valid @RequestBody OrderRequest request) {
        return ResponseEntity.ok(OrderResponse.from(reservationOrderService.createOrder(request.toCommand())));
    }

    @PostMapping("/{orderId}/fail")
    public ResponseEntity<Void> failOrder(@PathVariable String orderId) {
        orderService.failOrder(orderId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{orderId}")
    public ResponseEntity<OrderResponse> getOrder(@PathVariable String orderId) {
        return ResponseEntity.ok(OrderResponse.from(orderService.getOrderResult(orderId)));
    }
}
