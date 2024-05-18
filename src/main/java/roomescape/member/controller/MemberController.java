package roomescape.member.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import roomescape.member.dto.MemberCreateRequest;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.member.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberProfileInfo> findMembers() {
        return memberService.findAllMembers();
    }

    @PostMapping
    public ResponseEntity<Void> createMember(@RequestBody MemberCreateRequest request) {
        MemberProfileInfo response = memberService.createMember(request);

        URI location = URI.create("/members/" + response.id());
        return ResponseEntity.created(location)
                .build();
    }
}
