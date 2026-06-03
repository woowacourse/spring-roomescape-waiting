package roomescape.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.controller.dto.LoginMemberResponse;
import roomescape.global.AdminOnly;
import roomescape.service.MemberService;

@AdminOnly
@RequestMapping("/admin/members")
@RestController
public class AdminMemberController {

    private final MemberService memberService;

    public AdminMemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public ResponseEntity<List<LoginMemberResponse>> findUsers() {
        return ResponseEntity.ok(memberService.findUsers());
    }
}
