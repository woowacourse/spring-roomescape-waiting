package roomescape.controller.api.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addAdminReservation(@RequestBody AdminReservationRequest request) {
        ReservationResponse reservationResponse = reservationService.addAdminReservation(request);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<MultipleResponse<ReservationResponse>> getAllReservations() {
        List<ReservationResponse> reservations = reservationService.getAllReservations();
        MultipleResponse<ReservationResponse> response = new MultipleResponse<>(reservations);

        return ResponseEntity.ok()
                .body(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<MultipleResponse<ReservationResponse>> getFilteredReservations(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<ReservationResponse> reservations
                = reservationService.getFilteredReservations(themeId, memberId, dateFrom, dateTo);
        MultipleResponse<ReservationResponse> response = new MultipleResponse<>(reservations);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id
    ) {
        reservationService.deleteById(id);

        return ResponseEntity.noContent()
                .build();
    }
}
