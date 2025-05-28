package roomescape.reservation.presentation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.presentation.dto.response.WaitingResponse;
import roomescape.reservationslot.presentation.dto.request.ConfirmedReservationCreateRequest;
import roomescape.reservationslot.presentation.dto.response.ReservationResponse;
import roomescape.reservation.application.ReservationApplicationService;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationResponse;

@RestController
public class ReservationController {

    private final ReservationApplicationService reservationApplicationService;

    public ReservationController(final ReservationApplicationService reservationApplicationService) {
        this.reservationApplicationService = reservationApplicationService;
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/waiting-reservations")
    public ResponseEntity<ReservationResponse> createWaitingReservation(
            @RequestBody ConfirmedReservationCreateRequest request,
            MemberInfo memberInfo
    ) {
        ReservationResponse reservationResponse = reservationApplicationService.createWaitingReservation(
                request.date(), request.timeId(), request.themeId(), memberInfo.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationResponse);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ConfirmedReservationResponse>> findByCriteria(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<ConfirmedReservationResponse> reservations = reservationApplicationService.findByCriteria(themeId,
                memberId, dateFrom, dateTo);
        return ResponseEntity.ok(reservations);
    }

    @RequireRole(MemberRole.ADMIN)
    @GetMapping("/admin/waiting-reservations")
    public ResponseEntity<List<WaitingResponse>> findWaitingReservations(
    ) {
        List<WaitingResponse> responses = reservationApplicationService.findWaitingReservations();
        return ResponseEntity.ok(responses);
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> removeConfirmedReservation(
            @PathVariable("reservationId") Long reservationId
    ) {
        reservationApplicationService.removeById(reservationId);
        return ResponseEntity.noContent().build();
    }

    @RequireRole(MemberRole.REGULAR)
    @DeleteMapping("/waiting-reservations/{reservationSlotId}")
    public ResponseEntity<Void> removeWaitingReservation(
            @PathVariable Long reservationSlotId,
            MemberInfo memberInfo
    ) {
        reservationApplicationService.removeWaitingReservation(reservationSlotId, memberInfo.id());
        return ResponseEntity.noContent().build();
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/admin/waiting-reservations/{waitingId}")
    public ResponseEntity<Void> removeWaitingReservation(
            @PathVariable Long waitingId
    ) {
        reservationApplicationService.removeWaitingReservationWithoutMemberId(waitingId);
        return ResponseEntity.noContent().build();
    }
}
