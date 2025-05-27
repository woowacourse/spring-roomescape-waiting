package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.Authenticated;
import roomescape.reservation.dto.request.AdminReservationRequest;
import roomescape.reservation.dto.request.ReservationCondition;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.dto.response.ReservationWithStatusResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/api/reservations")
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

    @PostMapping("/api/admin/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @Valid @RequestBody AdminReservationRequest request) {
        ReservationResponse reservationResponse = reservationService.createReservation(
                request.memberId(), request.timeId(), request.themeId(), request.date()
        );
        return ResponseEntity
                .created(URI.create("/api/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/api/admin/reservations")
    public List<ReservationResponse> getReservations(@ModelAttribute ReservationCondition condition) {
        return reservationService.findReservations(condition);
    }

    @GetMapping("/api/reservations/my")
    public List<ReservationWithStatusResponse> getMyReservation(@Authenticated Long id) {
        return reservationService.findReservationByMemberId(id);
    }

    @DeleteMapping("/api/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.cancelReservationAndPromoteWait(id);
        return ResponseEntity.noContent().build();
    }
}
