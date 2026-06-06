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
import static roomescape.member.domain.Role.MEMBER;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @AuthGuard(roles = {MEMBER, MANAGER})
    @PostMapping("/slots/{slotId}/reservations")
    public ResponseEntity<ReservationDetailDto> create(
            @PathVariable Long slotId,
            @LoginMember Member member
    ) {
        Reservation reservation = reservationService.reserve(member.getName(), slotId);
        ReservationDetailDto responseData = ReservationDetailDto.from(reservation);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = {MEMBER, MANAGER})
    @GetMapping("/my-reservations")
    public ResponseEntity<List<ReservationDetailDto>> getMyReservations(@LoginMember Member member) {
        List<ReservationDetailDto> responseData = reservationService.readAllByName(member.getName()).stream()
                .map(ReservationDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = {MEMBER, MANAGER})
    @GetMapping("/my-reservations/v2")
    public ResponseEntity<List<ReservationDetailDto>> getMyReservationsV2(@LoginMember Member member) {
        List<ReservationDetailDto> responseData = reservationService.readAllByNameV2(member.getName()).stream()
                .map(ReservationDetailDto::from)
                .toList();
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = {MEMBER, MANAGER})
    @PatchMapping("/reservations/{id}/cancel")
    public ResponseEntity<ReservationDetailDto> cancel(
            @PathVariable Long id,
            @LoginMember Member member
    ) {
        Reservation reservation = reservationService.cancel(id, member.getName());
        ReservationDetailDto responseData = ReservationDetailDto.from(reservation);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = {MEMBER, MANAGER})
    @PatchMapping("/reservations/{id}/schedule")
    public ResponseEntity<ReservationDetailDto> updateSchedule(
            @PathVariable Long id,
            @LoginMember Member member,
            @Validated @RequestBody ReservationChangeScheduleDto dto
    ) {
        Reservation reservation = reservationService.changeSchedule(dto.toCommand(id, member.getName()));
        ReservationDetailDto responseData = ReservationDetailDto.from(reservation);
        return ResponseEntity.ok(responseData);
    }

}
