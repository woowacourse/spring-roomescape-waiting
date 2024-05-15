package roomescape.controller.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.domain.Member;
import roomescape.dto.response.MemberPreviewResponse;
import roomescape.dto.response.MemberReservationResponse;
import roomescape.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<MemberPreviewResponse>> getMembers() {
        List<MemberPreviewResponse> response = memberService.getAllMemberPreview();

        return ResponseEntity.ok(response);
    }

    // TODO: 메서드명 재고
    @GetMapping("/reservations")
    public ResponseEntity<List<MemberReservationResponse>> getReservations(Member member) {
        List<MemberReservationResponse> response = memberService.getReservations(member);

        return ResponseEntity.ok(response);
    }
}
