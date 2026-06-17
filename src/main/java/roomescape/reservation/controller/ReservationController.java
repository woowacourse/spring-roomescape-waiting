package roomescape.reservation.controller;

import static roomescape.member.domain.Role.MANAGER;
import static roomescape.member.domain.Role.MEMBER;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.auth.annotation.AuthGuard;
import roomescape.common.auth.annotation.LoginMember;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.request.ReservationChangeScheduleDto;
import roomescape.reservation.controller.dto.request.ReservationSaveDto;
import roomescape.reservation.controller.dto.response.ReservationDetailDto;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @AuthGuard(roles = {MEMBER, MANAGER})
    @PostMapping("/reservations")
    public ResponseEntity<ReservationDetailDto> create(
        @Validated @RequestBody ReservationSaveDto dto,
        @LoginMember Member member
    ) {
        Reservation reservation = reservationService.reserve(member, dto.toCommand());
        ReservationDetailDto responseData = ReservationDetailDto.from(reservation);
        return ResponseEntity.ok(responseData);
    }

    @AuthGuard(roles = {MEMBER, MANAGER})
    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationDetailDto>> getMyReservations(
        @LoginMember Member member) {
        List<ReservationDetailDto> responseData = reservationService.readAllByMemberId(member.getId())
            .stream()
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
        Reservation reservation = reservationService.cancel(id, member);
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
        Reservation reservation = reservationService.changeSchedule(dto.toCommand(id, member));
        ReservationDetailDto responseData = ReservationDetailDto.from(reservation);
        return ResponseEntity.ok(responseData);
    }

}
