package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.AuthenticationPrincipal;
import roomescape.global.auth.annotation.RoleRequired;
import roomescape.global.auth.dto.LoginMember;
import roomescape.member.entity.RoleType;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationReadMemberResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    @RoleRequired(roleType = {RoleType.ADMIN, RoleType.USER})
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestBody @Valid ReservationCreateRequest request
    ) {
        ReservationCreateResponse response = reservationService.createReservation(loginMember.id(), request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/mine")
    @RoleRequired(roleType = {RoleType.ADMIN, RoleType.USER})
    public ResponseEntity<List<ReservationReadMemberResponse>> getMyReservations(
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        List<ReservationReadMemberResponse> responses = reservationService.getReservationsByMember(loginMember.id());
        return ResponseEntity.ok(responses);
    }
}
