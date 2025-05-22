package roomescape.member.ui;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import roomescape.common.response.ApiResponse;
import roomescape.member.application.MemberService;
import roomescape.member.application.dto.MemberRequest;
import roomescape.member.application.dto.MemberResponse;

@Controller
@RequestMapping("members")
@AllArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<ApiResponse<MemberResponse>> create(@Valid @RequestBody MemberRequest request) {
        MemberResponse response = memberService.create(request);
        ApiResponse<MemberResponse> apiResponse = ApiResponse.createSuccess(response);
        return ResponseEntity.ok(apiResponse);
    }
}
