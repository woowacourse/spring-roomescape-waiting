package roomescape.reservation.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.payment.controller.dto.response.PaymentReadyResponse;
import roomescape.reservation.controller.dto.request.ReservationCreateRequest;
import roomescape.reservation.controller.dto.response.ReservationOptionResponse;
import roomescape.reservation.controller.dto.response.ReservationsAndWaitingsResponse;
import roomescape.reservation.repository.dto.ReservationTimesWithStatus;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping(params = {"customer-name", "customer-email"})
    public ResponseEntity<ReservationsAndWaitingsResponse> getReservationsByCustomer(
            @RequestParam("customer-name") String customerName,
            @RequestParam("customer-email") String customerEmail
    ) {
        final ReservationsAndWaitingsResponse results = reservationService.getReservationsByCustomer(customerName, customerEmail);
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
    public ResponseEntity<PaymentReadyResponse> create(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        final PaymentReadyResponse result = reservationService.preparePayment(request);
        return ResponseEntity.created(URI.create("/reservations"))
                .body(result);
    }

    @DeleteMapping("/{reservation-id}")
    public ResponseEntity<Void> cancel(
            @PathVariable("reservation-id") Long reservationId,
            @RequestParam("customer-name") String customerName,
            @RequestParam("customer-email") String customerEmail
    ) {
        reservationService.cancelByCustomer(reservationId, customerName, customerEmail);
        return ResponseEntity.noContent().build();
    }
}
