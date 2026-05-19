package roomescape.member.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.member.controller.dto.request.MemberSaveDto;
import roomescape.member.controller.dto.response.MemberDetailDto;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;

@RestController
@RequestMapping("/member")
@RequiredArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping("/members")
    public ResponseEntity<MemberDetailDto> register(@Valid @RequestBody MemberSaveDto dto) {
        Member member = memberService.register(dto.toCommand());
        MemberDetailDto responseData = MemberDetailDto.from(member);
        return ResponseEntity.ok(responseData);
    }

}
