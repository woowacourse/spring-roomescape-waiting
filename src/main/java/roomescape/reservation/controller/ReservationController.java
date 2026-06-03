package roomescape.reservation.controller;

import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.annotation.Authenticated;
import roomescape.auth.annotation.LoginName;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.ReservationUpdateRequest;
import roomescape.reservation.controller.dto.ReservationWithStatusResponse;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.exception.InvalidReservationRequestFormatException;
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
        Reservation reservation = reservationService.makeReservation(requestDto.toCommand());
        ReservationResponse response = ReservationResponse.from(reservation);

        return ResponseEntity
                .created(URI.create("/reservations/" + response.id()))
                .body(response);
    }


    @GetMapping
    public ResponseEntity<List<ReservationWithStatusResponse>> getAllReservationsByName(@RequestParam("name") String name) {
        if (name == null || name.isBlank()) {
            throw new InvalidReservationRequestFormatException();
        }

        List<ReservationWithStatusResponse> responses = reservationService.findReservationsByName(name)
                .stream()
                .map(ReservationWithStatusResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @Authenticated
    @PatchMapping("/{id}")
    public ResponseEntity<Void> updateMyReservation(
            @LoginName String name,
            @PathVariable Long id,
            @RequestBody ReservationUpdateRequest request
    ) {
        reservationService.updateReservation(request.toCommand(), id, name);
        return ResponseEntity.noContent().build();
    }

    @Authenticated
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMyReservation(
            @LoginName String name,
            @PathVariable Long id
    ) {
        reservationService.deleteReservationById(id, name);
        return ResponseEntity.noContent().build();
    }
}
