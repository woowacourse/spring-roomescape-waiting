package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.PaymentCancelRequest;
import roomescape.controller.dto.PaymentConfirmRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.service.UserReservationService;
import roomescape.service.dto.ReservationResult;

@RestController
@RequestMapping("/user/payments")
public class PaymentController {

    private final UserReservationService userReservationService;

    public PaymentController(UserReservationService userReservationService) {
        this.userReservationService = userReservationService;
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
