package roomescape.member.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.service.MemberService;
import roomescape.member.controller.request.SignUpRequest;
import roomescape.member.domain.Member;

@RestController
@RequestMapping("/members")
public class SignUpApiController {

    private final MemberService service;

    public SignUpApiController(MemberService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<Member> createUser(@RequestBody SignUpRequest request) {
        Member member = service.save(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(member);
    }
}
