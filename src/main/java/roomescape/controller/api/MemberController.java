package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.auth.SignUpRequestDto;
import roomescape.dto.member.MemberResponseDto;
import roomescape.dto.member.MemberSignupResponseDto;
import roomescape.service.MemberService;

import java.util.List;

@RestController
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping("/members")
    public ResponseEntity<List<MemberResponseDto>> getMembers() {
        List<MemberResponseDto> allMembers = memberService.findAllMembers();
        return ResponseEntity.ok(allMembers);
    }

    @PostMapping("/members")
    public ResponseEntity<MemberSignupResponseDto> signup(@RequestBody SignUpRequestDto requestDto) {
        MemberSignupResponseDto memberSignupResponseDto = memberService.registerMember(requestDto);
        return ResponseEntity.ok().body(memberSignupResponseDto);
    }
}
