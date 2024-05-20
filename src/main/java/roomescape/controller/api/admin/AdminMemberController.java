package roomescape.controller.api.admin;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.response.MemberPreviewResponse;
import roomescape.dto.response.MultipleResponse;
import roomescape.service.MemberService;

import java.util.List;

@RequestMapping("/admin/members")
@RestController
public class AdminMemberController {

    private final MemberService memberService;

    public AdminMemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<MultipleResponse<MemberPreviewResponse>> getMemberPreview() {
        List<MemberPreviewResponse> members = memberService.getAllMemberPreview();
        MultipleResponse<MemberPreviewResponse> response = new MultipleResponse<>(members);

        return ResponseEntity.ok(response);
    }
}
