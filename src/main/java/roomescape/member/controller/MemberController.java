package roomescape.member.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.request.MemberCreationRequest;
import roomescape.auth.dto.response.MemberCreationUpResponse;
import roomescape.member.dto.response.MemberNameResponse;
import roomescape.member.service.MemberServiceFacade;

@RestController
@AllArgsConstructor
public class MemberController {

    private final MemberServiceFacade memberService;

    @GetMapping("/members")
    public ResponseEntity<List<MemberNameResponse>> getMembers() {
        List<MemberNameResponse> memberNames = memberService.findNames();
        return ResponseEntity.ok(memberNames);
    }

    @PostMapping("/members")
    public ResponseEntity<MemberCreationUpResponse> signup(@RequestBody @Valid MemberCreationRequest request) {
        MemberCreationUpResponse response = memberService.create(request);
        return ResponseEntity.ok(response);
    }
}
