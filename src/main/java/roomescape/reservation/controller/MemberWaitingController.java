package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.MemberReservationCreateRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class MemberWaitingController {

    private final ReservationService reservationService;

    public MemberWaitingController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/waitings")
    public WaitingResponse createWaitingReservation(
            @Valid @RequestBody MemberReservationCreateRequest request,
            LoginMember member) {
        return reservationService.createWaitingReservation(request, member);
    }

    @DeleteMapping("/waitings/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@PathVariable Long id, LoginMember loginMember) {
        reservationService.deleteWaitingReservation(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
