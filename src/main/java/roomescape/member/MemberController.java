package roomescape.member;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.auth.AuthenticationPrincipal;
import roomescape.auth.dto.LoginMember;
import roomescape.member.dto.MemberRequest;
import roomescape.member.dto.MemberResponse;
import roomescape.reservation.ReservationWaitingService;
import roomescape.reservation.reservation.dto.ReservationAndWaitingResponse;

import java.util.List;

@RestController
@RequestMapping("/members")
@AllArgsConstructor
public class MemberController {

    private final MemberService memberService;
    private final ReservationWaitingService reservationWaitingService;

    @PostMapping
    public ResponseEntity<Void> createMember(
            @RequestBody @Valid MemberRequest request
    ) {
        memberService.createMember(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationAndWaitingResponse>> readAll(
            @AuthenticationPrincipal final LoginMember member
    ) {
        final List<ReservationAndWaitingResponse> response = reservationWaitingService.readAllByMember(member);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<MemberResponse>> readAllMember() {
        final List<MemberResponse> response = memberService.readAllMember();
        return ResponseEntity.ok(response);
    }

}
