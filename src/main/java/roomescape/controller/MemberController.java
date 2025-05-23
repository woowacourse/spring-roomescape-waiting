package roomescape.controller;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.RegisterMemberRequest;
import roomescape.controller.dto.response.MemberResponse;
import roomescape.controller.dto.response.RegisterUserResponse;
import roomescape.service.MemberService;
import roomescape.service.dto.param.RegisterMemberParam;
import roomescape.service.dto.result.MemberResult;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(final MemberService memberService) {
        this.memberService = memberService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public RegisterUserResponse signup(@Valid @RequestBody final RegisterMemberRequest registerMemberRequest) {
        RegisterMemberParam registerMemberParam = registerMemberRequest.toServiceParam();
        MemberResult memberResult = memberService.create(registerMemberParam);
        return RegisterUserResponse.from(memberResult);
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> getMembers() {
        List<MemberResponse> members = memberService.getAll().stream()
                .map(MemberResponse::from)
                .toList();
        
        return ResponseEntity.ok().body(members);
    }
}
