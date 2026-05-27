package roomescape.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.dto.request.ReservationCreateRequest;
import roomescape.reservation.service.dto.request.ReservationUpdateRequest;
import roomescape.reservation.service.dto.response.ReservationOptionResponse;
import roomescape.reservation.service.dto.response.ReservationResponse;
import roomescape.reservation.service.dto.response.ReservationsAndWaitingsResponse;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping(params = "customer-name")
    public ResponseEntity<ReservationsAndWaitingsResponse> getReservationsByCustomerName(
            @RequestParam("customer-name") String customerName
    ) {
        final ReservationsAndWaitingsResponse results = reservationService.getReservationsByCustomerName(customerName);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<ReservationTimesWithStatus>> getReservationTimeStatuses(
            @RequestParam(value = "date") LocalDate date,
            @RequestParam(value = "themeId") Long themeId
    ) {
        final List<ReservationTimesWithStatus> results = reservationService.getReservationTimeStatuses(date, themeId);
        return ResponseEntity.ok(results);
    }

    @GetMapping("/date-and-theme")
    public ResponseEntity<ReservationOptionResponse> getReservationOptions() {
        final ReservationOptionResponse results = reservationService.getReservationOptions();
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        final ReservationResponse result = reservationService.create(request);
        return ResponseEntity.created(URI.create("/reservations"))
                .body(result);
    }

    @PutMapping("/{reservation-id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable("reservation-id") Long reservationId,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        final ReservationResponse result = reservationService.updateByCustomer(reservationId, request);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{reservation-id}")
    public ResponseEntity<Void> cancel(
            @PathVariable("reservation-id") Long reservationId
    ) {
        reservationService.cancel(reservationId);
        return ResponseEntity.noContent().build();
    }
}
