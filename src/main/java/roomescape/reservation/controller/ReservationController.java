package roomescape.reservation.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.exception.InvalidRequestFormatException;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationWithStatusResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.ReservationErrorCode;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody ReservationRequest requestDto) {
        Reservation reservation = reservationService.save(requestDto.toCommand());
        ReservationResponse response = ReservationResponse.from(reservation);
        return ResponseEntity
                .created(URI.create("/reservations/" + response.id()))
                .body(response);
    }


    @GetMapping
    public ResponseEntity<List<ReservationWithStatusResponse>> getAllReservationsByName(
            @RequestParam("name") String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidRequestFormatException(ReservationErrorCode.INVALID_FORMAT.getMessage());
        }

        List<ReservationWithStatusResponse> responses = reservationService.findAllByName(name)
                .stream()
                .map(ReservationWithStatusResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }
}
