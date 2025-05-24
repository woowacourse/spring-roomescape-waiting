package roomescape.controller.api;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.MemberRequest;
import roomescape.dto.response.MemberResponse;
import roomescape.service.MemberService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    private MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberResponse> readMembers() {
        return memberService.findAllMembers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MemberResponse createMember(@RequestBody MemberRequest request) {
        return memberService.addMember(request);
    }
}
