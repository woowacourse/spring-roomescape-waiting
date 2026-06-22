package roomescape.payment.web;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.payment.config.PaymentProperties;
import roomescape.payment.web.dto.PaymentConfirmRequest;
import roomescape.payment.web.dto.PaymentFailRequest;
import roomescape.payment.web.dto.PaymentOrderCreateRequest;
import roomescape.payment.web.dto.PaymentOrdersResponse;
import roomescape.payment.web.dto.PaymentReadyResponse;
import roomescape.payment.service.PaymentReadyOrder;
import roomescape.payment.service.PaymentService;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.domain.Reservation;

import static org.springframework.http.HttpStatus.CREATED;

@RestController
@RequestMapping("/payments")
public class PaymentController {
    private final PaymentService paymentService;
    private final PaymentProperties paymentProperties;

    public PaymentController(PaymentService paymentService, PaymentProperties paymentProperties) {
        this.paymentService = paymentService;
        this.paymentProperties = paymentProperties;
    }

    @PostMapping("/orders")
    public ResponseEntity<PaymentReadyResponse> createOrder(
            @Valid @RequestBody PaymentOrderCreateRequest request
    ) {
        PaymentReadyOrder paymentReadyOrder = paymentService.prepare(
                request.name(),
                request.date(),
                request.timeId(),
                request.themeId()
        );

        return ResponseEntity.status(CREATED)
                .body(PaymentReadyResponse.from(paymentReadyOrder, paymentProperties));
    }

    @GetMapping("/orders")
    public ResponseEntity<PaymentOrdersResponse> findOrders(@RequestParam String name) {
        return ResponseEntity.ok(PaymentOrdersResponse.from(paymentService.findOrdersByName(name)));
    }

    @PostMapping("/confirm")
    public ResponseEntity<ReservationResponse> confirm(
            @Valid @RequestBody PaymentConfirmRequest request
    ) {
        Reservation reservation = paymentService.confirm(
                request.paymentKey(),
                request.orderId(),
                request.amount()
        );

        return ResponseEntity.ok(ReservationResponse.from(reservation));
    }

    @PostMapping("/fail")
    public ResponseEntity<Void> fail(@RequestBody PaymentFailRequest request) {
        paymentService.fail(request.orderId(), request.code(), request.message());
        return ResponseEntity.noContent().build();
    }
}
