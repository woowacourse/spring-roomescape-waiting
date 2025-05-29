package roomescape.reservation.presentation;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.application.WaitingReservationApplicationService;
import roomescape.reservation.presentation.dto.response.WaitingWebResponse;
import roomescape.reservation.presentation.dto.request.ConfirmedReservationCreateWebRequest;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;

@RestController
public class WaitingReservationController {

    private final WaitingReservationApplicationService waitingReservationApplicationService;

    public WaitingReservationController(
            final WaitingReservationApplicationService waitingReservationApplicationService) {
        this.waitingReservationApplicationService = waitingReservationApplicationService;
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/waiting-reservations")
    public ResponseEntity<ReservationResponse> create(
            @RequestBody ConfirmedReservationCreateWebRequest request,
            MemberInfo memberInfo
    ) {
        ReservationResponse reservationResponse = waitingReservationApplicationService.create(
                request.date(), request.timeId(), request.themeId(), memberInfo.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponse);
    }

    @RequireRole(MemberRole.ADMIN)
    @GetMapping("/admin/waiting-reservations")
    public ResponseEntity<List<WaitingWebResponse>> findAll(
    ) {
        List<WaitingWebResponse> responses = waitingReservationApplicationService.findAll();
        return ResponseEntity.ok(responses);
    }

    @RequireRole(MemberRole.REGULAR)
    @DeleteMapping("/waiting-reservations/{reservationSlotId}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long reservationSlotId,
            MemberInfo memberInfo
    ) {
        waitingReservationApplicationService.cancel(reservationSlotId, memberInfo.id());
        return ResponseEntity.noContent().build();
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/admin/waiting-reservations/{waitingId}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long waitingId
    ) {
        waitingReservationApplicationService.cancelWaitingReservationWithoutMemberId(waitingId);
        return ResponseEntity.noContent().build();
    }
}
