package roomescape.controller;

import java.time.LocalDateTime;
import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.controller.dto.ReservationResponse;
import roomescape.controller.dto.UserReservationRequest;
import roomescape.domain.Member;
import roomescape.global.auth.LoginMember;
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationWithWaitingOrder;

@RequestMapping("/reservations")
@RestController
@Validated
public class UserReservationController {

    private final ReservationService reservationService;

    public UserReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findMine(@LoginMember Member member) {
        LocalDateTime now = LocalDateTime.now();
        List<ReservationResponse> responses = reservationService.findByMember(member).stream()
                .map(result -> toResponse(result, now))
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<Void> createWaiting(
            @Valid @RequestBody UserReservationRequest request,
            @LoginMember Member member
    ) {
        Long reservationId = reservationService.saveWaitingReservation(request, member);
        return ResponseEntity.created(URI.create("/reservations/" + reservationId)).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancel(
            @PathVariable Long id,
            @LoginMember Member member
    ) {
        reservationService.cancelReservation(id, member);
        return ResponseEntity.noContent().build();
    }

    private ReservationResponse toResponse(ReservationWithWaitingOrder result, LocalDateTime now) {
        return ReservationResponse.from(result.reservation(), result.order(), now);
    }
}
