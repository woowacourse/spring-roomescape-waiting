package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.OrderHistoryResponse;
import roomescape.controller.dto.PaymentCancelRequest;
import roomescape.controller.dto.PaymentConfirmRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.payment.PaymentService;
import roomescape.service.UserReservationService;
import roomescape.service.dto.ReservationResult;

@RestController
@RequestMapping("/user/payments")
@Validated
public class PaymentController {

    private final UserReservationService userReservationService;
    private final PaymentService paymentService;

    public PaymentController(UserReservationService userReservationService, PaymentService paymentService) {
        this.userReservationService = userReservationService;
        this.paymentService = paymentService;
    }

    @GetMapping
    public List<OrderHistoryResponse> history(
            @RequestParam @NotBlank(message = "reserverName은 필수입니다.") String reserverName
    ) {
        return paymentService.findOrderHistories(reserverName).stream()
                .map(OrderHistoryResponse::from)
                .toList();
    }

    @PostMapping("/confirm")
    public ReservationResponse confirm(@RequestBody @Valid PaymentConfirmRequest request) {
        ReservationResult saved = userReservationService.confirm(request.toCommand());
        return ReservationResponse.from(saved);
    }

    @PostMapping("/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(@RequestBody PaymentCancelRequest request) {
        if (request.orderId() == null || request.orderId().isBlank()) {
            return;
        }
        userReservationService.cancelOrder(request.orderId());
    }
}
