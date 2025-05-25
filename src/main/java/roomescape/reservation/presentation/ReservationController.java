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
import roomescape.bookingslot.presentation.dto.request.ReservationCreateRequest;
import roomescape.bookingslot.presentation.dto.response.WaitingReservationResponse;
import roomescape.reservation.application.ReservationApplicationService;
import roomescape.reservation.presentation.dto.ReservationResponse;

@RestController
public class ReservationController {

    private final ReservationApplicationService reservationApplicationService;

    public ReservationController(final ReservationApplicationService reservationApplicationService) {
        this.reservationApplicationService = reservationApplicationService;
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/waiting-reservations")
    public ResponseEntity<WaitingReservationResponse> addWaitingReservations(
            @RequestBody ReservationCreateRequest request,
            MemberInfo memberInfo
    ) {
        WaitingReservationResponse waitingReservationResponse = reservationApplicationService.addWaiting(
                request.date(), request.timeId(), request.themeId(), memberInfo.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(waitingReservationResponse);
    }

    @RequireRole(MemberRole.REGULAR)
    @DeleteMapping("/waiting-reservations/{reservationId}")
    public ResponseEntity<Void> deleteWaitingReservations(
            @PathVariable Long reservationId,
            MemberInfo memberInfo
    ) {
        reservationApplicationService.removeWaiting(reservationId, memberInfo.id());
        return ResponseEntity.noContent().build();
    }

    @RequireRole(MemberRole.ADMIN)
    @GetMapping("/admin/waiting-reservations")
    public ResponseEntity<List<ReservationResponse>> findAllWaitingReservations(
    ) {
        List<ReservationResponse> responses = reservationApplicationService.findAllWaitingReservations();
        return ResponseEntity.ok(responses);
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/admin/waiting-reservations/{waitingId}")
    public ResponseEntity<Void> removeWaitingReservation(
            @PathVariable Long waitingId
    ) {
        reservationApplicationService.removeWaitingReservation(waitingId);
        return ResponseEntity.noContent().build();
    }
}
