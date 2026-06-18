package roomescape.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.service.ReservationApplicationService;
import roomescape.reservation.controller.dto.request.ReservationCreateRequest;
import roomescape.reservation.controller.dto.request.ReservationUpdateRequest;
import roomescape.reservation.service.dto.response.ReservationOptionResponse;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.reservation.service.dto.response.ReservationsAndWaitingsResponse;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationApplicationService reservationApplicationService;

    @GetMapping(params = "customer-name")
    public ResponseEntity<ReservationsAndWaitingsResponse> findReservationsByCustomerName(
        @RequestParam("customer-name") String customerName
    ) {
        final ReservationsAndWaitingsResponse response = reservationApplicationService
            .findReservationsAndWaitingsByCustomerName(customerName);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<ReservationTimesWithStatus>> getReservationTimeStatuses(
            @RequestParam(value = "date") LocalDate date,
            @RequestParam(value = "themeId") Long themeId
    ) {
        final List<ReservationTimesWithStatus> results = reservationApplicationService.findReservationTimeStatuses(date, themeId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/date-and-theme")
    public ResponseEntity<ReservationOptionResponse> getReservationOptions() {
        final ReservationOptionResponse results = reservationApplicationService.getReservationOptions();
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        final ReservationResponse result = reservationApplicationService.create(request);
        return ResponseEntity.created(URI.create("/reservations"))
                .body(result);
    }

    @PutMapping("/{reservation-id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable("reservation-id") Long reservationId,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        final ReservationResponse result = reservationApplicationService.updateByCustomer(reservationId, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{reservation-id}")
    public ResponseEntity<Void> cancel(
            @PathVariable("reservation-id") Long reservationId
    ) {
        reservationApplicationService.cancelReservationByIdAndPromoteWaiting(reservationId);
        return ResponseEntity.noContent().build();
    }
}
