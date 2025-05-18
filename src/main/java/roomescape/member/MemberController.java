package roomescape.member;


import jakarta.validation.Valid;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.config.AuthenticationPrincipal;
import roomescape.auth.dto.LoginMember;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberReservationResponse;
import roomescape.member.dto.MemberResponse;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class MemberController {

    private final MemberService memberService;

    @PostMapping
    public ResponseEntity<Void> createMember(
            @RequestBody @Valid MemberRequest request
    ) {
        memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<MemberReservationResponse>> readAllMemberWithReservations(
            @AuthenticationPrincipal final LoginMember loginMember
    ) {
        final List<MemberReservationResponse> response = memberService.readAllReservationsByMember(loginMember);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> readAllMember() {
        final List<MemberResponse> response = memberService.readAllMember();
        return ResponseEntity.ok(response);
    }

}
