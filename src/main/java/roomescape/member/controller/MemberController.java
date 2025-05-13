package roomescape.member.controller;

import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.dto.request.MemberCreateRequest;
import roomescape.member.dto.response.MemberGetResponse;
import roomescape.member.service.MemberService;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    public MemberGetResponse signUp(@RequestBody @Valid  MemberCreateRequest requestBody) {
        return MemberGetResponse.from(memberService.createUser(requestBody));
    }

    @GetMapping
    public List<MemberGetResponse> readAllMembers() {
        return memberService.findAllMembers().stream()
                .map(MemberGetResponse::from)
                .toList();
    }
}
