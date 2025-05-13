package roomescape.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.MemberService;
import roomescape.application.dto.MemberCreateDto;
import roomescape.application.dto.MemberDto;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @GetMapping
    public List<MemberDto> getAllMembers() {
        return memberService.getAllMembers();
    }

    @PostMapping
    public ResponseEntity<MemberDto> createMember(@Valid @RequestBody MemberCreateDto memberCreateDto) {
        MemberDto memberDto = memberService.registerMember(memberCreateDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(memberDto);
    }
}
