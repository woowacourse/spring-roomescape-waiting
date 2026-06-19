package roomescape.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.controller.dto.payment.PaymentOrderHistoryResponse;
import roomescape.service.PaymentService;

@RequestMapping("/admin/payments")
@RestController
public class AdminPaymentController {

    private final PaymentService paymentService;

    public AdminPaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @GetMapping("/orders")
    public ResponseEntity<List<PaymentOrderHistoryResponse>> findOrders() {
        List<PaymentOrderHistoryResponse> responses = paymentService.findAllOrders().stream()
                .map(PaymentOrderHistoryResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }
}
