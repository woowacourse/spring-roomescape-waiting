package roomescape.reservation.presentation;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
import roomescape.reservation.presentation.dto.response.ConfirmedReservationResponse;
import roomescape.reservationslot.presentation.dto.request.AdminReservationSlotCreateRequest;
import roomescape.reservationslot.presentation.dto.request.ConfirmedReservationCreateRequest;
import roomescape.reservationslot.presentation.dto.response.MyReservationResponse;

@RestController
public class ConfirmedReservationController {

    private final ConfirmedReservationApplicationService confirmedReservationApplicationService;

    public ConfirmedReservationController(
            final ConfirmedReservationApplicationService confirmedReservationApplicationService) {
        this.confirmedReservationApplicationService = confirmedReservationApplicationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ConfirmedReservationResponse>> findByCriteria(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<ConfirmedReservationResponse> reservations = confirmedReservationApplicationService.findByCriteria(themeId,
                memberId, dateFrom, dateTo);
        return ResponseEntity.ok(reservations);
    }

    @RequireRole(MemberRole.ADMIN)
    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> cancel(
            @PathVariable("reservationId") Long reservationId
    ) {
        confirmedReservationApplicationService.cancel(reservationId);
        return ResponseEntity.noContent().build();
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/reservations")
    public ResponseEntity<ConfirmedReservationResponse> create(
            @RequestBody ConfirmedReservationCreateRequest request,
            MemberInfo memberInfo
    ) {
        ConfirmedReservationResponse response = confirmedReservationApplicationService.create(
                request.date(), request.timeId(), request.themeId(), memberInfo.id(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping("/admin/reservations")
    public ResponseEntity<ConfirmedReservationResponse> create(
            @RequestBody AdminReservationSlotCreateRequest request
    ) {
        ConfirmedReservationResponse dto = confirmedReservationApplicationService.create(
                request.date(),
                request.timeId(), request.themeId(), request.memberId(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @RequireRole(MemberRole.REGULAR)
    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponse>> findMine(MemberInfo memberInfo) {
        List<MyReservationResponse> myReservations = confirmedReservationApplicationService.findMyReservations(
                memberInfo);
        return ResponseEntity.ok().body(myReservations);
    }
}
