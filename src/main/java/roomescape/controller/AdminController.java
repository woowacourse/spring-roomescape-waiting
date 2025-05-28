package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.request.CreateReservationAdminRequest;
import roomescape.service.ReservationService;
import roomescape.service.param.CreateReservationParam;
import roomescape.service.result.ReservationResult;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReservationService reservationService;

    public AdminController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResult> createReservation(@RequestBody CreateReservationAdminRequest reservationRequest) {
        CreateReservationParam createReservationParam = new CreateReservationParam(
                reservationRequest.memberId(),
                reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId(),
                reservationRequest.status()
        );
        Long reservationId = reservationService.create(createReservationParam, LocalDateTime.now());
        return ResponseEntity.ok().body(reservationService.findById(reservationId));
    }
}
