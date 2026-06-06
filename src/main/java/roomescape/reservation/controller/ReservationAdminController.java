package roomescape.reservation.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.common.auth.annotation.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.request.ReservationChangeScheduleDto;
import roomescape.reservation.controller.dto.response.ReservationDetailDto;
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
    public ResponseEntity<List<ReservationDetailDto>> getReservations() {
        List<ReservationDetailDto> responseData = reservationService.readAll().stream()
                .map(ReservationDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PostMapping("/slots/{slotId}/reservations")
    public ResponseEntity<ReservationDetailDto> createReservation(
            @PathVariable Long slotId,
            @LoginMember Member manager
    ) {
        Reservation reservation = reservationService.reserve(manager.getName(), slotId);
        ReservationDetailDto responseData = ReservationDetailDto.from(reservation);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PatchMapping("/reservations/{id}/cancel")
    public ResponseEntity<ReservationDetailDto> cancelReservation(@PathVariable Long id) {
        Reservation reservation = reservationService.cancelByManager(id);
        ReservationDetailDto responseData = ReservationDetailDto.from(reservation);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = MANAGER)
    @PatchMapping("/reservations/{id}/schedule")
    public ResponseEntity<ReservationDetailDto> updateSchedule(
            @PathVariable Long id,
            @Validated @RequestBody ReservationChangeScheduleDto dto
    ) {
        Reservation reservation = reservationService.changeScheduleByManager(dto.toCommand(id));
        ReservationDetailDto responseData = ReservationDetailDto.from(reservation);
        return ResponseEntity.ok(responseData);
    }

}
