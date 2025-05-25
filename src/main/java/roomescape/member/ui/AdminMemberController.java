package roomescape.member.ui;

import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.response.ApiResponse;
import roomescape.member.application.MemberService;
import roomescape.member.application.dto.MemberResponse;

@RestController
@RequestMapping("admin/members")
@AllArgsConstructor
public class AdminMemberController {
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getAll() {
        ApiResponse<List<MemberResponse>> apiResponse = ApiResponse.createSuccess(memberService.findAll());
        return ResponseEntity.ok(apiResponse);
    }
}
