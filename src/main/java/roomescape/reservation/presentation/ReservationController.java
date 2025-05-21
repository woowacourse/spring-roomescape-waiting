package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.Authenticated;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationWithStatusResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping(value = "/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping()
    public ResponseEntity<ReservationResponse> createNewReservation(
            @Authenticated Long memberId,
            @Valid @RequestBody ReservationRequest request) {
        ReservationResponse reservationResponse = reservationService.createReservation(
                memberId, request.timeId(), request.themeId(), request.date()
        );
        return ResponseEntity
                .created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservationById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public List<ReservationWithStatusResponse> getMyReservation(@Authenticated Long id) {
        return reservationService.findReservationByMemberId(id);
    }
}
