package roomescape.member.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import roomescape.member.controller.dto.request.MemberRequest;
import roomescape.member.controller.dto.response.MemberResponse;
import roomescape.member.service.MemberService;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse createMember(@RequestBody @Valid MemberRequest request) {
        return MemberResponse.from(memberService.addMember(request));
    }

    @GetMapping
    public List<MemberResponse> readMembers() {
        return memberService.findAllMembers().stream()
                .map(MemberResponse::from)
                .toList();
    }
}
