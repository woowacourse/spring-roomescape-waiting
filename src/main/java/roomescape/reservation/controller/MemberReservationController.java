package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.ReservationService;

import java.util.List;

@RestController
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ReservationResponse createReservation(
            @Valid @RequestBody MemberReservationCreateRequest request,
            LoginMember member) {
        return reservationService.createReservation(request, member);
    }

    @PostMapping("/waitings")
    public WaitingResponse createWaitingReservation(
            @Valid @RequestBody MemberReservationCreateRequest request,
            LoginMember member) {
        return reservationService.createWaitingReservation(request, member);
    }

    @GetMapping("/reservations/mine")
    public List<MemberReservationResponse> readMemberReservations(LoginMember loginMember) {
        return reservationService.readMemberReservations(loginMember);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@PathVariable Long id, LoginMember loginMember) {
        reservationService.deleteWaitingReservation(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
