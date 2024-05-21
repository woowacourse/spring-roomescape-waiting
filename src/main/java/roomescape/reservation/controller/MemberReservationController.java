package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public MemberReservationResponse createReservation(
            @Valid @RequestBody MemberReservationCreateRequest request,
            LoginMember member
    ) {
        return reservationService.createReservation(request, member);
    }

    @GetMapping("/my")
    public List<MyReservationResponse> readMemberReservations(LoginMember loginMember) {
        return reservationService.readMemberReservations(loginMember);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemberReservation(@PathVariable Long id, LoginMember loginMember) {
        reservationService.deleteReservation(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
