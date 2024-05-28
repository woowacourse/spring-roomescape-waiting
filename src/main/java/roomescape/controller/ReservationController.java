package roomescape.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import roomescape.domain.Member;
import roomescape.dto.MemberReservationRequest;
import roomescape.dto.MemberReservationResponse;
import roomescape.dto.ReservationResponse;
import roomescape.service.MemberService;
import roomescape.service.ReservationService;

@RequestMapping("/reservations")
@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final MemberService memberService;

    public ReservationController(ReservationService reservationService, MemberService memberService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
    }

    @GetMapping("/my")
    public ResponseEntity<List<MemberReservationResponse>> getMyReservations(Member member) {
        List<MemberReservationResponse> response = memberService.getReservationsOf(member);
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody MemberReservationRequest request,
            Member member
    ) {
        ReservationResponse response = reservationService.addMemberReservation(request, member);
        URI location = URI.create("/reservations/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping
    public ResponseEntity<Void> checkReservationExists(
            @RequestParam LocalDate date,
            @RequestParam Long timeId,
            @RequestParam Long themeId
    ) {
        boolean exists = reservationService.checkReservationExists(date, timeId, themeId);
        if (!exists) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok().build();
    }
}
