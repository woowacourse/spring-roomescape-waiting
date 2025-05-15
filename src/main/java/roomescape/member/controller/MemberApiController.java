package roomescape.member.controller;

import static roomescape.member.controller.response.MemberSuccessCode.GET_MEMBERS;
import static roomescape.member.controller.response.MemberSuccessCode.SIGN_UP;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.response.ApiResponse;
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.service.MemberService;

@RequiredArgsConstructor
@RestController
public class MemberApiController {

    private final MemberService memberService;

    @PostMapping("/members")
    public ResponseEntity<ApiResponse<MemberResponse>> signUp(@RequestBody @Valid SignUpRequest request) {
        MemberResponse response = memberService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(SIGN_UP, response));
    }

    @GetMapping("/members")
    public ResponseEntity<ApiResponse<List<MemberResponse>>> getMembers() {
        List<MemberResponse> responses = memberService.getMembers();

        return ResponseEntity.ok(
                ApiResponse.success(GET_MEMBERS, responses));
    }
}
