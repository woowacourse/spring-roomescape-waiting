package roomescape.reservation.controller;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.LoginMemberId;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reservationSave(@RequestBody ReservationRequest reservationRequest,
                                                               @LoginMemberId long id) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(reservationService.addReservation(reservationRequest, id));
    }

    @GetMapping
    public List<ReservationResponse> reservaionList() {
        return reservationService.findReservations();
    }

    @GetMapping("/{themeId}")
    public List<ReservationTimeAvailabilityResponse> reservationTimeList(@PathVariable long themeId,
                                                                         @RequestParam LocalDate date) {
        return reservationService.findTimeAvailability(themeId, date);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> reservationRemove(@PathVariable long reservationId) {
        reservationService.removeReservations(reservationId);
        return ResponseEntity.noContent().build();
    }
}
