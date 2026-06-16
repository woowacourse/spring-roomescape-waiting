package roomescape.controller.member;

import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.ConfirmResponse;
import roomescape.controller.dto.response.PaymentResponse;
import roomescape.domain.order.Order;
import roomescape.domain.order.OrderRepository;
import roomescape.infrastructure.payment.toss.TossPaymentException;
import roomescape.infrastructure.payment.toss.dto.TossErrorResponse;
import roomescape.service.PaymentService;
import roomescape.service.dto.result.PaymentResult;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private static final long DEFAULT_AMOUNT = 50_000L;
    private static final String ORDER_NAME = "방탈출 예약 — 우아한 비밀의 방";

    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final String clientKey;

    public PaymentController(
            OrderRepository orderRepository,
            PaymentService paymentService,
            @Value("${toss.client-key}") String clientKey
    ) {
        this.orderRepository = orderRepository;
        this.paymentService = paymentService;
        this.clientKey = clientKey;
    }

    @GetMapping
    public ResponseEntity<PaymentResponse> checkout() {
        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");

        orderRepository.save(new Order(orderId, DEFAULT_AMOUNT));

        PaymentResponse response = new PaymentResponse(
                clientKey,
                orderId,
                ORDER_NAME,
                DEFAULT_AMOUNT
        );

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/success")
    public ResponseEntity<ConfirmResponse> success(
            @RequestParam String paymentKey,
            @RequestParam String orderId,
            @RequestParam Long amount
    ) {
        PaymentResult result = paymentService.confirm(paymentKey, orderId, amount);

        ConfirmResponse response = new ConfirmResponse(
                result.paymentKey(),
                result.orderId(),
                result.status().name(),
                result.approvedAmount()
        );

        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/fail")
    public void fail(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String message,
            @RequestParam(required = false) String orderId
    ) {
        TossErrorResponse response = new TossErrorResponse(code, message);
        throw TossPaymentException.of(HttpStatus.BAD_REQUEST, response);
    }
}
