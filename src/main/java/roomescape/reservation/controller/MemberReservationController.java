package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.facade.ReservationFacadeService;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class MemberReservationController {

    private final ReservationFacadeService reservationFacadeService;

    public MemberReservationController(ReservationFacadeService reservationFacadeService) {
        this.reservationFacadeService = reservationFacadeService;
    }

    @PostMapping
    public MemberReservationResponse createReservation(
            @Valid @RequestBody MemberReservationCreateRequest request,
            LoginMember member
    ) {
        return reservationFacadeService.createReservation(request, member);
    }

    @GetMapping("/my")
    public List<MyReservationResponse> readMemberReservations(LoginMember loginMember) {
        return reservationFacadeService.readMemberReservations(loginMember);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMemberReservation(@PathVariable Long id, LoginMember loginMember) {
        reservationFacadeService.deleteReservation(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
