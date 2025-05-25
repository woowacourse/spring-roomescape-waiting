package roomescape.controller.member;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.MemberRegisterRequest;
import roomescape.dto.response.MemberRegisterResponse;
import roomescape.dto.response.MemberResponse;
import roomescape.dto.response.MyPageReservationResponse;
import roomescape.service.member.MemberService;
import roomescape.service.reservation.ReservationService;

@RequiredArgsConstructor
@RestController
public class MemberController {

    private final MemberService memberService;
    private final ReservationService reservationService;

    @PostMapping("/members")
    public ResponseEntity<MemberRegisterResponse> registerMember(@RequestBody final MemberRegisterRequest request) {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.addMember(request));
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponse>> getAllMembers() {
        return ResponseEntity.status(HttpStatus.OK).body(memberService.getAllMembers());
    }

    @GetMapping("/members/reservations")
    public ResponseEntity<List<MyPageReservationResponse>> getMyReservations(final Long memberId) {
        List<MyPageReservationResponse> reservations = reservationService.getReservationsByMemberId(memberId);
        return ResponseEntity.ok(reservations);
    }
}
