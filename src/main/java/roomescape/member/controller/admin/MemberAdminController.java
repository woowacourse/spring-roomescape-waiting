package roomescape.member.controller.admin;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.Admin;
import roomescape.global.dto.response.ApiResponse;
import roomescape.member.dto.MembersResponse;
import roomescape.member.service.MemberService;

@RestController
public class MemberAdminController {
    private final MemberService memberService;

    public MemberAdminController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @Admin
    @GetMapping("/admin/members")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<MembersResponse> getAllMembers() {
        return ApiResponse.success(memberService.findAllMembers());
    }
}
