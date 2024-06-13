package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.reservation.dto.request.ReservationCreateAdminRequest;
import roomescape.domain.Status;
import roomescape.service.ReservationService;
import roomescape.service.dto.request.ReservationCreateRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.ReservationWaitingResponse;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findReservations() {
        return ResponseEntity.ok(reservationService.findAll());
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ReservationResponse>> findBy(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo)
    {
        return ResponseEntity.ok().body(reservationService.findBy(themeId, memberId, dateFrom, dateTo));
    }

    @GetMapping("/admin/reservations/waiting")
    public ResponseEntity<List<ReservationWaitingResponse>> findReservationsWaiting() {
        return ResponseEntity.ok().body(reservationService.findReservationsWaiting());
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> create(
     @Valid @RequestBody ReservationCreateAdminRequest adminRequest)
    {
        ReservationCreateRequest reservationCreateRequest = ReservationCreateRequest.of(adminRequest, Status.CREATED);
        ReservationResponse reservationResponse = reservationService.save(reservationCreateRequest);
        return ResponseEntity.created(URI.create("/admin/reservation")).body(reservationResponse);
    }

    @DeleteMapping("/admin/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
