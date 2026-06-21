package roomescape.controller;

import java.net.URI;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ReservationRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.controller.dto.response.MyReservationResponse;
import roomescape.controller.dto.response.MyReservationsResponse;
import roomescape.controller.dto.response.ReservationPaymentResponse;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.controller.dto.response.ReservationWaitingResponse;
import roomescape.controller.dto.response.ReservationWaitingsResponse;
import roomescape.controller.dto.response.ReservationsResponse;
import roomescape.domain.MyReservation;
import roomescape.domain.PaymentStatus;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationPayment;
import roomescape.domain.ReservationType;
import roomescape.service.ReservationPaymentService;
import roomescape.service.ReservationService;

@RequestMapping("/reservations")
@RestController
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationPaymentService reservationPaymentService;
    private final String tossClientKey;

    public ReservationController(
            ReservationService reservationService,
            ReservationPaymentService reservationPaymentService,
            @Value("${toss.client-key}") String tossClientKey
    ) {
        this.reservationService = reservationService;
        this.reservationPaymentService = reservationPaymentService;
        this.tossClientKey = tossClientKey;
    }

    @GetMapping
    public ResponseEntity<ReservationsResponse> getReservations(@RequestParam String username) {
        List<ReservationResponse> responses = reservationService.findAllByName(username)
                .stream()
                .map(r -> ReservationResponse.fromReserved(r, r.getTheme()))
                .toList();
        return ResponseEntity.ok(new ReservationsResponse(responses));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.save(
                request.name(), request.date(), request.timeId(), request.themeId());
        ReservationResponse response = ReservationResponse.fromReserved(reservation, reservation.getTheme());
        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @PostMapping("/payment")
    public ResponseEntity<ReservationPaymentResponse> createReservationPayment(
            @Valid @RequestBody ReservationRequest request
    ) {
        ReservationPayment payment = reservationPaymentService.prepare(
                request.name(), request.date(), request.timeId(), request.themeId());
        ReservationPaymentResponse response = ReservationPaymentResponse.from(payment, tossClientKey);
        URI location = URI.create("/reservations/payment/" + response.orderId());
        return ResponseEntity.created(location).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable long id, @RequestParam String username,
            @Valid @RequestBody ReservationUpdateRequest request) {
        Reservation reservation = reservationService.update(id, username, request.date(), request.timeId());
        return ResponseEntity.ok(ReservationResponse.fromReserved(reservation, reservation.getTheme()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable long id, @RequestParam String username) {
        reservationService.delete(id, username);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> createReservationWaiting(@Valid @RequestBody ReservationRequest request) {
        Reservation reservation = reservationService.saveWaiting(
                request.name(), request.date(), request.timeId(), request.themeId());
        ReservationResponse response = ReservationResponse.fromWaiting(reservation, reservation.getTheme());
        URI location = URI.create("/reservations/waiting/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteReservationWaiting(@PathVariable long id, @RequestParam String username) {
        reservationService.deleteWaiting(id, username);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/waiting")
    public ResponseEntity<ReservationWaitingsResponse> getReservationWaiting(@RequestParam String username) {
        List<ReservationWaitingResponse> responses = reservationService.findAllWaitingByName(username)
                .stream()
                .map(r -> ReservationWaitingResponse.from(r.reservation(), r.reservation().getTheme(), r.waitingNumber()))
                .toList();
        return ResponseEntity.ok(new ReservationWaitingsResponse(responses));
    }

    @GetMapping("/me")
    public ResponseEntity<MyReservationsResponse> getMyReservations(@RequestParam String username) {
        List<ReservationPayment> payments = reservationPaymentService.findPaymentsByName(username);
        Map<String, ReservationPayment> paymentsBySlot = payments.stream()
                .collect(java.util.stream.Collectors.toMap(
                        payment -> slotKey(payment.getReservation()),
                        payment -> payment,
                        (first, second) -> first
                ));
        List<MyReservation> myReservations = reservationService.getMyReservations(username).stream()
                .map(myReservation -> {
                    if (myReservation.reservationType() != ReservationType.CONFIRMED) {
                        return myReservation;
                    }
                    return myReservation.withPayment(paymentsBySlot.get(slotKey(myReservation.reservation())));
                })
                .toList();
        List<MyReservation> pendingPayments = payments.stream()
                .filter(payment -> payment.getPaymentStatus() != PaymentStatus.CONFIRMED)
                .map(MyReservation::payment)
                .toList();
        List<MyReservationResponse> responses = java.util.stream.Stream.concat(
                        myReservations.stream(),
                        pendingPayments.stream()
                )
                .sorted(Comparator
                        .comparing((MyReservation r) -> r.reservation().getDate())
                        .thenComparing(r -> r.reservation().getTime().getStartAt())
                        .thenComparing(MyReservation::reservationType))
                .map(MyReservationResponse::from)
                .toList();
        return ResponseEntity.ok(new MyReservationsResponse(responses));
    }

    private String slotKey(Reservation reservation) {
        return reservation.getDate() + "|" + reservation.getTime().getStartAt() + "|" + reservation.getTheme().getId();
    }
}
