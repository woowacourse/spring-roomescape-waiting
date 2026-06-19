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
import static roomescape.member.domain.Role.MEMBER;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @AuthGuard(roles = {MEMBER, MANAGER})
    @PostMapping("/slots/{slotId}/reservations")
    public ResponseEntity<ReservationWithSlotDetailDto> create(
            @PathVariable Long slotId,
            @LoginMember Member member
    ) {
        ReservationWithSlotDetailDto responseData = reservationService.reserve(member.getName(), slotId);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = {MEMBER, MANAGER})
    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationWithSlotDetailDto>> getMyReservationsV2(@LoginMember Member member) {
        List<ReservationWithSlotDetailDto> responseData = reservationService.readAllByName(member.getName()).stream()
                .map(ReservationWithSlotDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = {MEMBER, MANAGER})
    @PatchMapping("/slots/{slotId}/reservations/{reservationId}/cancel")
    public ResponseEntity<ReservationDetailDto> cancel(
            @PathVariable Long slotId,
            @PathVariable Long reservationId,
            @LoginMember Member member
    ) {
        Reservation canceled = reservationService.cancel(slotId, reservationId, member.getName());
        ReservationDetailDto responseData = ReservationDetailDto.from(canceled);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = {MEMBER, MANAGER})
    @PatchMapping("/slots/{slotId}/reservations/{reservationId}/reschedule")
    public ResponseEntity<ReservationDetailDto> updateSchedule(
            @PathVariable Long slotId,
            @PathVariable Long reservationId,
            @LoginMember Member member,
            @Validated @RequestBody ReservationRescheduleDto dto
    ) {
        Reservation rescheduled = reservationService.reschedule(slotId, dto.newSlotId(), reservationId, member.getName());
        ReservationDetailDto responseData = ReservationDetailDto.from(rescheduled);
        return ResponseEntity.ok(responseData);
    }

}
