package roomescape.web.controller;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import roomescape.service.MemberService;
import roomescape.web.dto.request.member.SignupRequest;
import roomescape.web.dto.response.member.MemberResponse;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping
    public ResponseEntity<List<MemberResponse>> findAllMember() {
        List<MemberResponse> allMember = memberService.findAllMember();
        return ResponseEntity.ok(allMember);
    }

    @PostMapping
    public ResponseEntity<Void> signup(@Valid @RequestBody SignupRequest request) {
        long createdId = memberService.signup(request);
        return ResponseEntity.created(URI.create("/members/" + createdId)).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> withdrawal(@PathVariable("id") String id) {
        memberService.withdrawal(Long.valueOf(id));
        return ResponseEntity.noContent()
                .build();
    }
}
