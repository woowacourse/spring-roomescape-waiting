package roomescape.reservation.presentation.controller.regular;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.application.WaitingReservationApplicationService;
import roomescape.reservation.application.dto.request.WaitingReservationCreateRequest;
import roomescape.reservation.presentation.dto.request.ConfirmedReservationCreateWebRequest;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;

@RestController
@RequestMapping("/waiting-reservations")
public class RegularWaitingReservationController {

    private final WaitingReservationApplicationService waitingReservationApplicationService;

    public RegularWaitingReservationController(
            final WaitingReservationApplicationService waitingReservationApplicationService) {
        this.waitingReservationApplicationService = waitingReservationApplicationService;
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody ConfirmedReservationCreateWebRequest request,
            MemberInfo memberInfo
    ) {
        ReservationResponse reservationResponse = waitingReservationApplicationService.create(
                new WaitingReservationCreateRequest(request.date(), request.timeId(), request.themeId(),
                        memberInfo.id()));
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponse);
    }

    @RequireRole(MemberRole.REGULAR)
    @DeleteMapping("/{reservationSlotId}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long reservationSlotId,
            MemberInfo memberInfo
    ) {
        waitingReservationApplicationService.cancelByReservationSlotIdAndMemberId(reservationSlotId, memberInfo.id());
        return ResponseEntity.noContent().build();
    }
}
