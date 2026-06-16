package roomescape.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.PaymentReadyResponse;
import roomescape.domain.Order;
import roomescape.service.OrderService;

@RestController
@RequestMapping("/user/payments")
public class PaymentController {

    private static final long DEFAULT_AMOUNT = 50_000L;
    private static final String ORDER_NAME = "방탈출 예약";

    private final OrderService orderService;
    private final String clientKey;

    public PaymentController(
            OrderService orderService,
            @Value("${toss.client-key:}") String clientKey
    ) {
        this.orderService = orderService;
        this.clientKey = clientKey;
    }

    @PostMapping("/ready")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentReadyResponse ready() {
        Order order = orderService.create(DEFAULT_AMOUNT);
        return PaymentReadyResponse.from(clientKey, order, ORDER_NAME);
    }
}
