package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.Member;
import roomescape.dto.request.MemberReservationRequest;
import roomescape.dto.response.MemberReservationResponse;
import roomescape.dto.response.MultipleResponse;
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

    public ReservationController( //todo: 인수가 여러개일 때 줄바꿈 형식 통일하기
                                  ReservationService reservationService,
                                  MemberService memberService
    ) {
        this.reservationService = reservationService;
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<MultipleResponse<MemberReservationResponse>> getReservationsOf(Member member) {
        List<MemberReservationResponse> reservations = memberService.getReservationsOf(member);
        MultipleResponse<MemberReservationResponse> response = new MultipleResponse<>(reservations);

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
