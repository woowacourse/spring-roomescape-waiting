package roomescape.waiting.presentation;

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
import roomescape.reservation.presentation.dto.request.ReservationCreateRequest;
import roomescape.reservation.presentation.dto.response.WaitingReservationResponse;
import roomescape.waiting.application.WaitingApplicationService;
import roomescape.waiting.presentation.dto.WaitingResponse;

@RestController
public class WaitingController {

    private final WaitingApplicationService waitingApplicationService;

    public WaitingController(final WaitingApplicationService waitingApplicationService) {
        this.waitingApplicationService = waitingApplicationService;
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/waiting-reservations")
    public ResponseEntity<WaitingReservationResponse> addWaitingReservations(
            @RequestBody ReservationCreateRequest request,
            MemberInfo memberInfo
    ) {
        WaitingReservationResponse waitingReservationResponse = waitingApplicationService.addWaiting(
                request.date(), request.timeId(), request.themeId(), memberInfo.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(waitingReservationResponse);
    }

    @RequireRole(MemberRole.REGULAR)
    @DeleteMapping("/waiting-reservations/{reservationId}")
    public ResponseEntity<Void> deleteWaitingReservations(
            @PathVariable Long reservationId,
            MemberInfo memberInfo
    ) {
        waitingApplicationService.removeWaiting(reservationId, memberInfo.id());
        return ResponseEntity.noContent().build();
    }

    @RequireRole(MemberRole.ADMIN)
    @GetMapping("/admin/waiting-reservations")
    public ResponseEntity<List<WaitingResponse>> findAllWaitingReservations(
    ) {
        List<WaitingResponse> responses = waitingApplicationService.findAllWaitingReservations();
        return ResponseEntity.ok(responses);
    }
}
