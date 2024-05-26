package roomescape.controller.reservationwaiting;

import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.auth.LoginMember;
import roomescape.controller.auth.RoleAllowed;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.service.reservationwaiting.ReservationWaitingService;
import roomescape.service.reservationwaiting.dto.ReservationWaitingListResponse;
import roomescape.service.reservationwaiting.dto.ReservationWaitingRequest;
import roomescape.service.reservationwaiting.dto.ReservationWaitingResponse;

@RestController
public class ReservationWaitingController {
    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @RoleAllowed(value = MemberRole.ADMIN) // TODO: value 명시 안하는 쪽으로 통일하기
    @GetMapping("/reservations/waitings")
    public ResponseEntity<ReservationWaitingListResponse> findAllReservationWaiting() {
        ReservationWaitingListResponse response = reservationWaitingService.findAllReservationWaiting();
        return ResponseEntity.ok().body(response);
    }

    @RoleAllowed
    @PostMapping("/reservations/waitings")
    public ResponseEntity<ReservationWaitingResponse> saveReservationWaiting(
            @RequestBody ReservationWaitingRequest request, @LoginMember Member member) {
        ReservationWaitingResponse response = reservationWaitingService.saveReservationWaiting(request, member);
        return ResponseEntity.created(URI.create("/reservations/waitings/" + response.getId())).body(response);
    }

    @RoleAllowed
    @DeleteMapping("/reservations/{reservationId}/waitings")
    public ResponseEntity<Void> deleteReservation(@PathVariable Long reservationId, @LoginMember Member member) {
        reservationWaitingService.deleteReservationWaiting(reservationId, member);
        return ResponseEntity.noContent().build();
    }
}
