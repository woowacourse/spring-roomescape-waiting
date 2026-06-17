package roomescape.reservation.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.BookingResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationUpdateRequest;
import roomescape.reservation.service.BookingResult;
import roomescape.reservation.service.ReservationService;

import java.net.URI;
import java.util.List;

@Tag(name = "예약", description = "예약 생성·조회·수정·삭제 API")
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationResponse>> getMyReservations(@LoginMember Member member) {
        List<ReservationResponse> responses = reservationService.getReservationsByMember(member).stream()
                .map(ReservationResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/bookings")
    public ResponseEntity<BookingResponse> book(@LoginMember Member member,
                                                @Valid @RequestBody ReservationRequest request) {
        BookingResult result = reservationService.book(member, request);
        BookingResponse response = result.isWaiting()
                ? BookingResponse.waiting(result.waiting())
                : BookingResponse.reserved(result.reservation());
        String location = result.isWaiting()
                ? "/waitings/" + result.waiting().getId()
                : "/bookings/" + result.reservation().getId();
        return ResponseEntity.created(URI.create(location)).body(response);
    }

    @PatchMapping("/bookings/{id}")
    public ResponseEntity<ReservationResponse> updateReservation(
            @LoginMember Member member,
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        return ResponseEntity.ok(ReservationResponse.from(reservationService.updateReservation(id, member, request)));
    }

    @DeleteMapping("/bookings/{id}")
    public ResponseEntity<Void> deleteReservation(@LoginMember Member member, @PathVariable Long id) {
        reservationService.deleteReservation(id, member);
        return ResponseEntity.noContent().build();
    }
}
