package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.dto.CreateReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> create(@RequestBody @Valid final CreateReservationRequest request) {
        final ReservationResponse response = reservationService.createReservation(request);
        return ResponseEntity.created(URI.create("/reservations/" + response.id())).body(response);
    }

    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ReservationResponse>> search(
            @RequestParam(value = "memberId") final Long memberId,
            @RequestParam(value = "themeId") final Long themeId,
            @RequestParam(value = "dateFrom") final LocalDate dateFrom,
            @RequestParam(value = "dateTo") final LocalDate dateTo
    ) {
        final List<ReservationResponse> responses = reservationService.getFilteredReservations(
                memberId,
                themeId,
                dateFrom,
                dateTo
        );
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        reservationService.cancelReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
