package roomescape.controller.api;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.annotation.AdminMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.auth.SignUpRequestDto;
import roomescape.dto.member.MemberResponseDto;
import roomescape.dto.member.MemberSignupResponseDto;
import roomescape.service.command.MemberCommandService;
import roomescape.service.query.MemberQueryService;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberQueryService memberQueryService;
    private final MemberCommandService memberCommandService;

    public MemberController(MemberQueryService memberQueryService, MemberCommandService memberCommandService) {
        this.memberQueryService = memberQueryService;
        this.memberCommandService = memberCommandService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<MemberResponseDto> getMembers(
            @AdminMember LoginInfo loginInfo
    ) {
        return memberQueryService.findAllMembers();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    public MemberSignupResponseDto signup(
            @RequestBody SignUpRequestDto requestDto
    ) {
        return memberCommandService.registerMember(requestDto);
    }
}
