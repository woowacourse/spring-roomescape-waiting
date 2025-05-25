package roomescape.reservationslot.presentation;

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
import roomescape.reservationslot.application.ReservationSlotApplicationService;
import roomescape.reservationslot.presentation.dto.request.AdminReservationSlotCreateRequest;
import roomescape.reservationslot.presentation.dto.request.ReservationSlotCreateRequest;
import roomescape.reservationslot.presentation.dto.response.MyReservationSlotResponse;
import roomescape.reservationslot.presentation.dto.response.ReservationSlotResponse;

@RestController
public class ReservationSlotController {

    private final ReservationSlotApplicationService reservationSlotApplicationService;

    public ReservationSlotController(final ReservationSlotApplicationService reservationSlotApplicationService) {
        this.reservationSlotApplicationService = reservationSlotApplicationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationSlotResponse>> findReservations(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<ReservationSlotResponse> reservations = reservationSlotApplicationService.findReservations(themeId, memberId,
                dateFrom, dateTo);
        return ResponseEntity.ok(reservations);
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/reservations")
    public ResponseEntity<ReservationSlotResponse> createReservation(
            @RequestBody ReservationSlotCreateRequest request,
            MemberInfo memberInfo
    ) {
        ReservationSlotResponse dto = reservationSlotApplicationService.create(request.date(), request.timeId(),
                request.themeId(),
                memberInfo.id(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationSlotResponse> createReservation(
            @RequestBody AdminReservationSlotCreateRequest request
    ) {
        ReservationSlotResponse dto = reservationSlotApplicationService.create(request.date(), request.timeId(),
                request.themeId(),
                request.memberId(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @RequireRole(MemberRole.REGULAR)
    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservations(
            @PathVariable("id") Long id
    ) {
        reservationSlotApplicationService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @RequireRole(MemberRole.REGULAR)
    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationSlotResponse>> findMyReservations(MemberInfo memberInfo) {
        List<MyReservationSlotResponse> myReservations = reservationSlotApplicationService.findMyReservations(memberInfo);
        return ResponseEntity.ok().body(myReservations);
    }
}
