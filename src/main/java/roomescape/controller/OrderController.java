package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.order.response.OrderResponse;
import roomescape.infrastructure.LoginRequired;
import roomescape.service.OrderService;

@RestController
@LoginRequired
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/order")
    public ResponseEntity<OrderResponse> getOrderByReservationId(@RequestParam Long reservationId) {
        return ResponseEntity.ok(OrderResponse.from(orderService.getByReservationId(reservationId)));
    }
}
