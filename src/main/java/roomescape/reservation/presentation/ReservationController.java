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
import roomescape.reservation.presentation.dto.request.AdminReservationCreateRequest;
import roomescape.reservation.presentation.dto.request.ReservationCreateRequest;
import roomescape.reservation.presentation.dto.response.MyReservationResponse;
import roomescape.reservation.presentation.dto.response.ReservationResponse;
import roomescape.reservation.application.ReservationApplicationService;

@RestController
public class ReservationController {

    private final ReservationApplicationService reservationApplicationService;

    public ReservationController(final ReservationApplicationService reservationApplicationService) {
        this.reservationApplicationService = reservationApplicationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findReservations(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        return ResponseEntity.ok(reservationApplicationService.findReservations(themeId, memberId, dateFrom, dateTo));
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody ReservationCreateRequest request,
            MemberInfo memberInfo
    ) {
        ReservationResponse dto = reservationApplicationService.create(request.date(), request.timeId(),
                request.themeId(),
                memberInfo.id(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> createReservation(
            @RequestBody AdminReservationCreateRequest request
    ) {
        ReservationResponse dto = reservationApplicationService.create(request.date(), request.timeId(),
                request.themeId(),
                request.memberId(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @RequireRole(MemberRole.REGULAR)
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservations(
            @PathVariable("id") Long id
    ) {
        reservationApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequireRole(MemberRole.REGULAR)
    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(MemberInfo memberInfo) {
        List<MyReservationResponse> myReservations = reservationApplicationService.findMyReservations(memberInfo);
        return ResponseEntity.ok().body(myReservations);
    }

}
