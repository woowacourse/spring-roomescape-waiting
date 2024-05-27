package roomescape.member.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.controller.dto.MemberResponse;
import roomescape.member.service.MemberService;

import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final MemberService memberService;

    public AdminController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public List<MemberResponse> findAll() {
        return memberService.findAll();
    }
}
