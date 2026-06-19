package roomescape.reservation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
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
import roomescape.member.domain.Member;
import roomescape.payment.service.PaymentService;
import roomescape.reservation.dto.BookingResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.service.BookingResult;
import roomescape.reservation.service.ReservationService;

@Tag(name = "예약", description = "예약 생성·조회·수정·삭제 API")
@RestController
@RequestMapping("/bookings")
public class ReservationController {

    private final ReservationService reservationService;
    private final PaymentService paymentService;
    private final String clientKey;

    public ReservationController(ReservationService reservationService,
                                PaymentService paymentService,
                                @Value("${toss.client-key:}") String clientKey) {
        this.reservationService = reservationService;
        this.paymentService = paymentService;
        this.clientKey = clientKey;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getMyReservations(@LoginMember Member member) {
        List<ReservationResponse> responses = reservationService.getReservationsByMember(member).stream()
                .map(reservation -> ReservationResponse.of(
                        reservation, paymentService.getPendingOrderId(reservation.getId())))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<BookingResponse> book(@LoginMember Member member,
                                                @Valid @RequestBody ReservationRequest request) {
        BookingResult result = reservationService.book(member, request);
        BookingResponse response = result.isWaiting()
                ? BookingResponse.waiting(result.waiting())
                : BookingResponse.pendingPayment(result.reservation(), result.orderId(), result.amount(), clientKey);
        String location = result.isWaiting()
                ? "/waitings/" + result.waiting().getId()
                : "/bookings/" + result.reservation().getId();
        return ResponseEntity.created(URI.create(location)).body(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @LoginMember Member member,
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        return ResponseEntity.ok(ReservationResponse.from(reservationService.updateReservation(id, member, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(@LoginMember Member member, @PathVariable Long id) {
        reservationService.deleteReservation(id, member);
        return ResponseEntity.noContent().build();
    }
}
