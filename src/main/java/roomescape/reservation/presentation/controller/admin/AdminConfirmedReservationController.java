package roomescape.reservation.presentation.controller.admin;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.application.ConfirmedReservationApplicationService;
import roomescape.reservation.application.dto.request.ConfirmedReservationByCriteriaWebRequest;
import roomescape.reservation.application.dto.request.ConfirmedReservationCreateRequest;
import roomescape.reservation.presentation.dto.request.AdminReservationSlotCreateWebRequest;
import roomescape.reservation.presentation.dto.response.ConfirmedReservationWebResponse;

@RestController
@RequestMapping("/admin/reservations")
public class AdminConfirmedReservationController {

    private final ConfirmedReservationApplicationService confirmedReservationApplicationService;

    public AdminConfirmedReservationController(
            final ConfirmedReservationApplicationService confirmedReservationApplicationService) {
        this.confirmedReservationApplicationService = confirmedReservationApplicationService;
    }

    @RequireRole(MemberRole.ADMIN)
    @GetMapping
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
    @DeleteMapping("/{reservationId}")
    public ResponseEntity<Void> cancel(
            @PathVariable("reservationId") Long reservationId
    ) {
        confirmedReservationApplicationService.cancel(reservationId);
        return ResponseEntity.noContent().build();
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping
    public ResponseEntity<ConfirmedReservationWebResponse> create(
            @RequestBody AdminReservationSlotCreateWebRequest request
    ) {
        ConfirmedReservationWebResponse dto = confirmedReservationApplicationService.create(
                ConfirmedReservationCreateRequest.of(request));
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }
}
