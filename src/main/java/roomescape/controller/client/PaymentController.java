package roomescape.controller.client;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.client.dto.request.ReservationRequest;
import roomescape.controller.client.dto.response.OrderHistoryResponse;
import roomescape.controller.client.dto.response.PreparePaymentResponse;
import roomescape.service.PaymentService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/prepare")
    public ResponseEntity<PreparePaymentResponse> prepare(@RequestBody @Valid ReservationRequest request) {
        PreparePaymentResponse response = paymentService.prepare(request.toCommand());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<OrderHistoryResponse>> getOrders(@RequestParam String name) {
        List<OrderHistoryResponse> responses = paymentService.getOrderHistories(name).stream()
                .map(OrderHistoryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
