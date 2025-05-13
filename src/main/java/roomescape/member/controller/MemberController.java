package roomescape.member.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.RoleRequired;
import roomescape.member.dto.request.MemberRequest.MemberCreateRequest;
import roomescape.member.dto.response.MemberResponse.MemberCreateResponse;
import roomescape.member.dto.response.MemberResponse.MemberReadResponse;
import roomescape.member.entity.RoleType;
import roomescape.member.service.MemberService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<MemberCreateResponse> createMember(
            @RequestBody @Valid MemberCreateRequest request
    ) {
        MemberCreateResponse response = memberService.createMember(request);
        return ResponseEntity.ok().body(response);
    }

    @GetMapping
    public ResponseEntity<List<MemberReadResponse>> getAllMembers() {
        List<MemberReadResponse> responses = memberService.getAllMembers();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/{id}")
    @RoleRequired(roleType = RoleType.ADMIN)
    public ResponseEntity<Void> deleteMember(
            @PathVariable("id") long id
    ) {
        memberService.deleteMember(id);
        return ResponseEntity.ok().build();
    }
}
