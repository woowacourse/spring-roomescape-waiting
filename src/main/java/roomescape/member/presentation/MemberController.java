package roomescape.member.presentation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.member.application.MemberService;
import roomescape.member.domain.Member;
import roomescape.member.dto.request.MemberJoinRequest;
import roomescape.member.dto.response.MemberResponse;

import java.util.List;

import static roomescape.member.domain.Role.ADMIN;
import static roomescape.member.domain.Role.USER;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
    }

    @PostMapping("/join")
    public ResponseEntity<MemberResponse> join(@RequestBody @Valid MemberJoinRequest request) {
        Member member = memberService.create(request.toModel(USER));
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(member));
    }

    @PostMapping("/join/admin")
    public ResponseEntity<MemberResponse> joinAdmin(@RequestBody @Valid MemberJoinRequest request) {
        Member member = memberService.create(request.toModel(ADMIN));
        return ResponseEntity.status(HttpStatus.CREATED).body(MemberResponse.from(member));
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> findAll() {
        List<MemberResponse> memberResponses = memberService.findAll()
                .stream()
                .map(MemberResponse::from)
                .toList();
        return ResponseEntity.ok(memberResponses);
    }
}
