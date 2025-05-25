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
import roomescape.reservation.waiting.application.WaitingReservationApplicationService;

@RestController
public class MemberWaitingController {

    private final WaitingReservationApplicationService waitingReservationApplicationService;

    public MemberWaitingController(WaitingReservationApplicationService waitingReservationApplicationService) {
        this.waitingReservationApplicationService = waitingReservationApplicationService;
    }

    @PostMapping("/reservation-waiting")
    public ResponseEntity<WaitingReservationResponse> createWaitingReservation(@RequestBody final ReservationRequest request,
                                                                               @LoginMember final LoginMemberInfo memberInfo) {
        WaitingReservationResponse response = waitingReservationApplicationService.createWaitingReservation(request, memberInfo.id());
        return ResponseEntity.ok().body(response);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteWaitingReservation(@LoginMember LoginMemberInfo loginMemberInfo,
                                                         @PathVariable("id") Long reservationId) {
        waitingReservationApplicationService.deleteByIdWithMemberId(loginMemberInfo.id(), reservationId);
        return ResponseEntity.noContent().build();
    }
}
