package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.Member;
import roomescape.dto.request.MemberReservationRequest;
import roomescape.dto.response.MultipleResponse;
import roomescape.dto.response.MyReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;

import java.net.URI;
import java.util.List;

@RequestMapping("/reservations")
@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberService memberService;

    public ReservationController(
            ReservationService reservationService,
            MemberService memberService
    ) {
        this.reservationService = reservationService;
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<MultipleResponse<MyReservationResponse>> getMyReservations(Member member) {
        List<MyReservationResponse> reservations = memberService.getMyReservations(member);
        MultipleResponse<MyReservationResponse> response = new MultipleResponse<>(reservations);

        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody MemberReservationRequest request, Member member) {
        ReservationResponse response = reservationService.addMemberReservation(request, member);
        URI location = URI.create("/reservations/" + response.id());

        return ResponseEntity.created(location)
                .body(response);
    }
}
