package roomescape.member.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.core.AuthenticationPrincipal;
import roomescape.auth.domain.AuthInfo;
import roomescape.member.dto.response.FindReservationResponse;
import roomescape.member.dto.response.FindWaitingResponse;
import roomescape.member.service.MemberService;
import roomescape.reservation.dto.response.FindMembersResponse;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<FindMembersResponse>> getMembers() {
        return ResponseEntity.ok(memberService.getMembers());
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<FindReservationResponse>> getReservationsByMember(@AuthenticationPrincipal AuthInfo authInfo) {
        return ResponseEntity.ok(memberService.getReservationsByMember(authInfo));
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<FindWaitingResponse>> getWaitingsByMember(@AuthenticationPrincipal AuthInfo authInfo) {
        return ResponseEntity.ok(memberService.getWaitingsByMember(authInfo));
    }
}
