package roomescape.user.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.controller.response.MemberResponse;
import roomescape.member.resolver.LoginMember;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.user.controller.dto.ReservationRequest;
import roomescape.user.controller.dto.response.MemberReservationResponse;

@RestController
public class UserReservationApiController {

    private final ReservationService reservationService;

    public UserReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> createReservation(@LoginMember MemberResponse memberResponse,
                                                                 @RequestBody ReservationRequest request) {
        ReservationResponse response = reservationService.createByName(memberResponse.name(), request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<ReservationResponse> responses = reservationService.getAll();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(
            @LoginMember MemberResponse memberResponse) {
        List<MemberReservationResponse> allByMemberId = reservationService.findAllByMemberId(memberResponse.id());
        return ResponseEntity.ok().body(allByMemberId);
    }
}
