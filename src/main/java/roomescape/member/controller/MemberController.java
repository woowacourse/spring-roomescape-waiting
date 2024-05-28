package roomescape.member.controller;

import org.springframework.http.HttpRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.system.auth.annotation.Admin;
import roomescape.system.dto.response.ApiResponse;
import roomescape.member.dto.MembersResponse;
import roomescape.member.service.MemberService;

@Controller
public class MemberController {
    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @RequestMapping(method = RequestMethod.GET, value = "/test")
    public ResponseEntity<Void> test() {
        return ResponseEntity.ok().build();
    }

    @Admin
    @GetMapping("/members")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<MembersResponse> getAllMembers() {
        return ApiResponse.success(memberService.findAllMembers());
    }
}
