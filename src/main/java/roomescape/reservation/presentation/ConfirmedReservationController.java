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
import roomescape.reservation.application.ConfirmedReservationApplicationService;
import roomescape.reservation.application.dto.request.ConfirmedReservationByCriteriaWebRequest;
import roomescape.reservation.application.dto.request.ConfirmedReservationCreateRequest;
import roomescape.reservation.presentation.dto.request.AdminReservationSlotCreateWebRequest;
import roomescape.reservation.presentation.dto.request.ConfirmedReservationCreateWebRequest;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationWebResponse;
import roomescape.reservationslot.presentation.dto.response.MyReservationResponse;

@RestController
public class ConfirmedReservationController {

    private final ConfirmedReservationApplicationService confirmedReservationApplicationService;

    public ConfirmedReservationController(
            final ConfirmedReservationApplicationService confirmedReservationApplicationService) {
        this.confirmedReservationApplicationService = confirmedReservationApplicationService;
    }

    @RequireRole(MemberRole.ADMIN)
    @GetMapping("/admin/reservations")
    public ResponseEntity<List<ConfirmedReservationWebResponse>> findByCriteria(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<ConfirmedReservationWebResponse> reservations = confirmedReservationApplicationService.findByCriteria(
                new ConfirmedReservationByCriteriaWebRequest(themeId, memberId, dateFrom, dateTo));
        return ResponseEntity.ok(reservations);
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/admin/reservations/{reservationId}")
    public ResponseEntity<Void> cancel(
            @PathVariable("reservationId") Long reservationId
    ) {
        confirmedReservationApplicationService.cancel(reservationId);
        return ResponseEntity.noContent().build();
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/reservations")
    public ResponseEntity<ConfirmedReservationWebResponse> create(
            @RequestBody ConfirmedReservationCreateWebRequest request,
            MemberInfo memberInfo
    ) {
        ConfirmedReservationWebResponse response = confirmedReservationApplicationService.create(
                ConfirmedReservationCreateRequest.of(request, memberInfo));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping("/admin/reservations")
    public ResponseEntity<ConfirmedReservationWebResponse> create(
            @RequestBody AdminReservationSlotCreateWebRequest request
    ) {
        ConfirmedReservationWebResponse dto = confirmedReservationApplicationService.create(
                ConfirmedReservationCreateRequest.of(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @RequireRole(MemberRole.REGULAR)
    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponse>> findMine(MemberInfo memberInfo) {
        List<MyReservationResponse> myReservations = confirmedReservationApplicationService.findMyReservations(
                memberInfo.id());
        return ResponseEntity.ok().body(myReservations);
    }
}
