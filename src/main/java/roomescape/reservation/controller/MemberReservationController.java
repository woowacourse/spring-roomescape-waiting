package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.annotation.AuthenticationPrincipal;
import roomescape.member.domain.Member;
import roomescape.reservation.controller.dto.request.ReservationSaveRequest;
import roomescape.reservation.controller.dto.response.MemberReservationResponse;
import roomescape.reservation.controller.dto.response.ReservationDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> save(
            @RequestBody @Valid final ReservationSaveRequest reservationSaveRequest,
            @AuthenticationPrincipal Member member
    ) {
        ReservationResponse reservationResponse =
                ReservationResponse.from(reservationService.save(reservationSaveRequest, member));
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAll() {
        List<ReservationResponse> reservationResponses = ReservationResponse.list(reservationService.getAll());
        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/me")
    public ResponseEntity<List<MemberReservationResponse>> findMemberReservations(
            @AuthenticationPrincipal Member member) {
        List<MemberReservationResponse> memberReservationResponses =
                MemberReservationResponse.list(reservationService.findByMemberId(member.getId()));
        return ResponseEntity.ok(memberReservationResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationDeleteResponse> delete(@PathVariable("id") final long id) {
        ReservationDeleteResponse reservationDeleteResponse =
                new ReservationDeleteResponse(reservationService.delete(id));
        return ResponseEntity.ok().body(reservationDeleteResponse);
    }
}
