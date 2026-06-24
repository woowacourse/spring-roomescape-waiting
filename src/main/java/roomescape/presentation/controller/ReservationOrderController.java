package roomescape.presentation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.OrderCreateUseCase;
import roomescape.application.payment.model.ReservationOrderResult;
import roomescape.presentation.dto.ReservationOrderRequest;
import roomescape.presentation.dto.ReservationOrderResponse;

@RestController
@RequestMapping("/reservation-orders")
public class ReservationOrderController {

    private final OrderCreateUseCase orderCreateUseCase;

    public ReservationOrderController(
            OrderCreateUseCase orderCreateUseCase
    ) {
        this.orderCreateUseCase = orderCreateUseCase;
    }

    @PostMapping
    public ResponseEntity<ReservationOrderResponse> create(
            @RequestBody @Valid ReservationOrderRequest request
    ) {
        ReservationOrderResult result = orderCreateUseCase.create(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationOrderResponse.from(result));
    }
}
