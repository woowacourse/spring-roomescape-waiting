package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.AuthenticationPrincipal;
import roomescape.global.auth.annotation.RoleRequired;
import roomescape.global.auth.dto.LoginMember;
import roomescape.member.entity.RoleType;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.request.ReservationReadFilteredRequest;
import roomescape.reservation.dto.response.ReservationCreateResponse;
import roomescape.reservation.dto.response.ReservationReadFilteredResponse;
import roomescape.reservation.dto.response.ReservationReadMemberResponse;
import roomescape.reservation.dto.response.ReservationReadResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationCreateResponse> createReservation(
            @AuthenticationPrincipal LoginMember loginMember,
            @RequestBody @Valid ReservationCreateRequest request
    ) {
        ReservationCreateResponse response = reservationService.createReservation(loginMember.id(), request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationReadResponse>> getAllReservations() {
        List<ReservationReadResponse> responses = reservationService.getAllReservations();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationReadMemberResponse>> getMyReservations(
            @AuthenticationPrincipal LoginMember loginMember
    ) {
        List<ReservationReadMemberResponse> responses = reservationService.getReservationsByMember(loginMember.id());
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<Void> deleteReservation(
            @PathVariable("id") long id
    ) {
        reservationService.deleteReservation(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/filtered")
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<List<ReservationReadFilteredResponse>> getFilteredReservations(
            @ModelAttribute @Valid ReservationReadFilteredRequest request
    ) {
        List<ReservationReadFilteredResponse> responses = reservationService.getFilteredReservations(request);
        return ResponseEntity.ok(responses);
    }
}
