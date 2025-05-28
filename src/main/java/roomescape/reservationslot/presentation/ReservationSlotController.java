package roomescape.reservationslot.presentation;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.security.annotation.RequireRole;
import roomescape.common.security.dto.request.MemberInfo;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.presentation.dto.response.TotalReservationResponse;
import roomescape.reservationslot.application.ReservationSlotApplicationService;
import roomescape.reservationslot.presentation.dto.request.AdminReservationSlotCreateRequest;
import roomescape.reservationslot.presentation.dto.request.ReservationSlotCreateRequest;
import roomescape.reservationslot.presentation.dto.response.MyReservationSlotResponse;

@RestController
public class ReservationSlotController {

    private final ReservationSlotApplicationService reservationSlotApplicationService;

    public ReservationSlotController(final ReservationSlotApplicationService reservationSlotApplicationService) {
        this.reservationSlotApplicationService = reservationSlotApplicationService;
    }

    @RequireRole(MemberRole.REGULAR)
    @PostMapping("/reservations")
    public ResponseEntity<TotalReservationResponse> createConfirmedReservation(
            @RequestBody ReservationSlotCreateRequest request,
            MemberInfo memberInfo
    ) {
        TotalReservationResponse response = reservationSlotApplicationService.createConfirmedReservation(request.date(),
                request.timeId(), request.themeId(), memberInfo.id(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RequireRole(MemberRole.ADMIN)
    @PostMapping("/admin/reservations")
    public ResponseEntity<TotalReservationResponse> createConfirmedReservation(
            @RequestBody AdminReservationSlotCreateRequest request
    ) {
        TotalReservationResponse dto = reservationSlotApplicationService.createConfirmedReservation(request.date(),
                request.timeId(), request.themeId(), request.memberId(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @RequireRole(MemberRole.REGULAR)
    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MyReservationSlotResponse>> findMyReservations(MemberInfo memberInfo) {
        List<MyReservationSlotResponse> myReservations = reservationSlotApplicationService.findMyReservations(
                memberInfo);
        return ResponseEntity.ok().body(myReservations);
    }
}
