package roomescape.reservation.ui;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.dto.AdminReservationRequest;
import roomescape.reservation.application.dto.ReservationResponse;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAllReserved() {
        return ResponseEntity.ok(reservationService.findReservedReservations());
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationResponse>> findAllWaiting() {
        return ResponseEntity.ok(reservationService.findWaitingReservations());
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> add(
            @Valid @RequestBody final AdminReservationRequest request
    ) {
        final ReservationResponse response = reservationService.addAdminReservation(request);
        return ResponseEntity.created(URI.create("/admin/reservations/" + response.id()))
                .body(response);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponse>> getFilteredReservations(
            @RequestParam(required = false, name = "themeId") final Long themeId,
            @RequestParam(required = false, name = "memberId") final Long memberId,
            @RequestParam(required = false, name = "from") final LocalDate start,
            @RequestParam(required = false, name = "to") final LocalDate end
    ) {
        final List<ReservationResponse> reservationResponses = reservationService.findReservationByThemeIdAndMemberIdInDuration(
                themeId, memberId, start, end);
        return ResponseEntity.ok(reservationResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Long id) {
        reservationService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/waiting/{id}")
    public ResponseEntity<Void> updateStatus(@PathVariable("id") final Long id) {
        reservationService.updateStatus(id);
        return ResponseEntity.ok().build();
    }

}
