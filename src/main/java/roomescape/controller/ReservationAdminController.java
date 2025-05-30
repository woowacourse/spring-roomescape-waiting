package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.request.CreateReservationAdminRequest;
import roomescape.controller.response.ReservationResponse;
import roomescape.service.ReservationService;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.ReservationResult;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin")
public class ReservationAdminController {

    private final ReservationService reservationService;

    public ReservationAdminController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/reservations")
    public ReservationResponse createReservation(@Valid @RequestBody CreateReservationAdminRequest reservationRequest) {
        CreateReservationParam createReservationParam = CreateReservationParam.from(reservationRequest);
        ReservationResult reservationResult = reservationService.create(createReservationParam, LocalDateTime.now());
        return ReservationResponse.from(reservationResult);
    }
}
