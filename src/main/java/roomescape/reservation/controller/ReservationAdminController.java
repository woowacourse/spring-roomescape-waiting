package roomescape.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.common.auth.annotation.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.request.ReservationRescheduleDto;
import roomescape.reservation.controller.dto.response.ReservationDetailDto;
import roomescape.reservation.controller.dto.response.ReservationWithSlotDetailDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationService;

import java.util.List;

import static roomescape.member.domain.Role.MANAGER;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
public class ReservationAdminController {

    private final ReservationService reservationService;

    @AuthGuard(roles = MANAGER)
    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationWithSlotDetailDto>> getReservations() {
        List<ReservationWithSlotDetailDto> responseData = reservationService.readAll().stream()
                .map(ReservationWithSlotDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PostMapping("/slots/{slotId}/reservations")
    public ResponseEntity<ReservationWithSlotDetailDto> createReservation(
            @PathVariable Long slotId,
            @LoginMember Member manager
    ) {
        ReservationWithSlotDetailDto responseData = reservationService.reserve(manager.getName(), slotId);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PatchMapping("/slots/{slotId}/reservations/{reservationId}/cancel")
    public ResponseEntity<ReservationDetailDto> cancelReservation(
            @PathVariable Long slotId,
            @PathVariable Long reservationId
    ) {
        Reservation canceled = reservationService.cancelByManager(slotId, reservationId);
        ReservationDetailDto responseData = ReservationDetailDto.from(canceled);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PatchMapping("/slots/{slotId}/reservations/{reservationId}/reschedule")
    public ResponseEntity<ReservationDetailDto> updateSchedule(
            @PathVariable Long slotId,
            @PathVariable Long reservationId,
            @Validated @RequestBody ReservationRescheduleDto dto
    ) {
        Reservation rescheduled = reservationService.rescheduleByManager(slotId, dto.newSlotId(), reservationId);
        ReservationDetailDto responseData = ReservationDetailDto.from(rescheduled);
        return ResponseEntity.ok(responseData);
    }

}
