package roomescape.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.request.CreateReservationAdminRequest;
import roomescape.controller.request.ReservationStatusRequest;
import roomescape.controller.response.ReservationWithRank;
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

    @GetMapping("/waitings")
    public ResponseEntity<List<ReservationWithRank>> getWaitingReservation() {
        return ResponseEntity.ok().body(reservationService.findAllWaitingsWithRank());
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResult> createReservation(@RequestBody CreateReservationAdminRequest reservationRequest) {
        CreateReservationParam createReservationParam = new CreateReservationParam(
                reservationRequest.memberId(),
                reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId()
        );
        Long reservationId = reservationService.create(createReservationParam, LocalDateTime.now());
        return ResponseEntity.ok().body(reservationService.findById(reservationId));
    }

    @PatchMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> changeReservationStatus(@PathVariable("reservationId") Long reservationId,
                                                        @RequestBody ReservationStatusRequest body) {
        reservationService.changeWaitingReservation(reservationId, body.status());
        return ResponseEntity.noContent().build();
    }
}
