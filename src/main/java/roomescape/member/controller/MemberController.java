package roomescape.member.controller;

import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingService;

import static java.util.Comparator.comparing;

@RestController
@RequestMapping("/member")
public class MemberController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public MemberController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(@CookieValue(name = "token", required = false) String token) {
        List<MemberReservationResponse> allMemberReservations = reservationService.findAllMemberReservations(token);
        List<MemberReservationResponse> allMemberWaitings = waitingService.findAllMemberWaitings(token);
        List<MemberReservationResponse> allReservations = Stream.of(allMemberReservations, allMemberWaitings)
                .flatMap(Collection::stream)
                .sorted(comparing(MemberReservationResponse::date)
                        .thenComparing(MemberReservationResponse::time))
                .toList();
        return ResponseEntity.ok(allReservations);
    }
}
