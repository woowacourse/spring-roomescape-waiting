package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.service.MemberReservationService;

@RestController
@RequestMapping("/member/reservations")
public class MemberReservationController {

    private final MemberReservationService memberReservationService;

    public MemberReservationController(MemberReservationService memberReservationService) {
        this.memberReservationService = memberReservationService;
    }

    @GetMapping
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(Member member) {
        List<MemberReservationResponse> allReservations = memberReservationService.findAll(member.getId());
        return ResponseEntity.ok(allReservations);
    }
}
