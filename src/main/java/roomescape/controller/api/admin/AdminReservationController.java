package roomescape.controller.api.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.request.AdminReservationRequest;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationCreationService;
import roomescape.service.ReservationDeletionService;
import roomescape.service.ReservationQueryService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final ReservationCreationService reservationCreationService;
    private final ReservationQueryService reservationQueryService;
    private final ReservationDeletionService reservationDeletionService;

    public AdminReservationController(
            ReservationCreationService reservationCreationService,
            ReservationQueryService reservationQueryService,
            ReservationDeletionService reservationDeletionService) {
        this.reservationCreationService = reservationCreationService;
        this.reservationQueryService = reservationQueryService;
        this.reservationDeletionService = reservationDeletionService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addAdminReservation(@RequestBody AdminReservationRequest request) {
        ReservationResponse reservationResponse = reservationCreationService.addReservationByAdmin(request);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<MultipleResponse<ReservationResponse>> getAllReservedReservations() {
        List<ReservationResponse> reservations = reservationQueryService.getAllReservedReservations();
        MultipleResponse<ReservationResponse> response = new MultipleResponse<>(reservations);

        return ResponseEntity.ok()
                .body(response);
    }

    @GetMapping("/waiting")
    public ResponseEntity<MultipleResponse<ReservationResponse>> getAllWaitingReservations() {
        List<ReservationResponse> reservations = reservationQueryService.getAllWaitingReservations();
        MultipleResponse<ReservationResponse> response = new MultipleResponse<>(reservations);

        return ResponseEntity.ok()
                .body(response);
    }

    @GetMapping("/filter")
    public ResponseEntity<MultipleResponse<ReservationResponse>> getFilteredReservations(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo) {
        List<ReservationResponse> reservations
                = reservationQueryService.getFilteredReservations(themeId, memberId, dateFrom, dateTo);
        MultipleResponse<ReservationResponse> response = new MultipleResponse<>(reservations);

        return ResponseEntity.ok(response);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long id) {
        reservationDeletionService.deleteById(id);

        return ResponseEntity.noContent()
                .build();
    }
}
