package roomescape.reservation.waiting.presentation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.login.presentation.dto.LoginMemberInfo;
import roomescape.auth.login.presentation.dto.annotation.LoginMember;
import roomescape.reservation.presentation.dto.ReservationRequest;
import roomescape.reservation.waiting.presentation.dto.WaitingReservationResponse;
import roomescape.reservation.waiting.application.WaitingReservationFacadeService;

@RestController
public class MemberWaitingController {

    private final WaitingReservationFacadeService waitingReservationFacadeService;

    public MemberWaitingController(WaitingReservationFacadeService waitingReservationFacadeService) {
        this.waitingReservationFacadeService = waitingReservationFacadeService;
    }

    @PostMapping("/reservation-waiting")
    public ResponseEntity<WaitingReservationResponse> createWaitingReservation(@RequestBody final ReservationRequest request,
                                                                               @LoginMember final LoginMemberInfo memberInfo) {
        WaitingReservationResponse response = waitingReservationFacadeService.createWaitingReservation(request, memberInfo.id());
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@LoginMember LoginMemberInfo loginMemberInfo,
                                                         @PathVariable("id") Long reservationId) {
        waitingReservationFacadeService.deleteByIdWithMemberId(loginMemberInfo.id(), reservationId);
        return ResponseEntity.noContent().build();
    }
}
