package roomescape.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.LoginMember;
import roomescape.payment.application.PaymentCheckout;
import roomescape.payment.application.PaymentService;
import roomescape.payment.ui.dto.PaymentCheckoutResponse;
import roomescape.payment.domain.exception.PaymentGatewayConfigurationException;
import roomescape.reservationwait.dto.WaitingResult;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.dto.MyReservationsAndWaitsResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservationwait.dto.WaitingResponse;
import roomescape.reservationwait.ReservationWaitService;

@RequestMapping("/api/v1/reservations")
@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final ReservationWaitService reservationWaitService;
    private final PaymentService paymentService;
    private final String paymentClientKey;

    public ReservationController(ReservationService reservationService,
                                 ReservationWaitService reservationWaitService,
                                 PaymentService paymentService,
                                 @Value("${payment.toss.client-key:}") String paymentClientKey) {
        this.reservationService = reservationService;
        this.reservationWaitService = reservationWaitService;
        this.paymentService = paymentService;
        this.paymentClientKey = paymentClientKey;
    }

    @GetMapping
    public ResponseEntity<MyReservationsAndWaitsResponse> getReservations(@LoginMember Long memberId) {
        List<Reservation> reservations = reservationService.getReservations(memberId);
        List<WaitingResult> waitingResponseResults = reservationWaitService.getWaitings(memberId);
        MyReservationsAndWaitsResponse myReservationsAndWaitsResponse = new MyReservationsAndWaitsResponse(
                ReservationResponse.fromAll(reservations), WaitingResponse.fromAll(waitingResponseResults));
        return ResponseEntity.ok().body(myReservationsAndWaitsResponse);
    }

    @PostMapping
    public ResponseEntity<PaymentCheckoutResponse> createReservation(
            @Valid @RequestBody ReservationCreateRequest reservationCreateRequest,
            @LoginMember Long memberId) {
        validatePaymentClientKey();
        PaymentCheckout checkout = paymentService.prepareReservation(
                memberId,
                reservationCreateRequest.date(),
                reservationCreateRequest.timeId(),
                reservationCreateRequest.themeId(),
                reservationCreateRequest.storeId()
        );
        PaymentCheckoutResponse response = PaymentCheckoutResponse.from(checkout, paymentClientKey);
        return ResponseEntity.created(URI.create("/api/v1/reservations/" + response.reservationId()))
                .body(response);
    }

    private void validatePaymentClientKey() {
        if (paymentClientKey == null || paymentClientKey.isBlank()) {
            throw new PaymentGatewayConfigurationException();
        }
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest reservationUpdateRequest,
            @LoginMember Long memberId) {
        Reservation updatedReservation = reservationService.updateReservation(
                id,
                reservationUpdateRequest.date(),
                memberId,
                reservationUpdateRequest.timeId()
        );
        ReservationResponse reservationResponse = ReservationResponse.from(updatedReservation);
        return ResponseEntity.ok().body(reservationResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable Long id, @LoginMember Long memberId) {
        reservationService.deleteReservation(id, memberId);
        return ResponseEntity.noContent().build();
    }
}
