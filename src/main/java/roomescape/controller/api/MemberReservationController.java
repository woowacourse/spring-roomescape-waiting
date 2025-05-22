package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.ReservationAndWaitingResponseDto;
import roomescape.service.reservation.ReservationQueryService;
import roomescape.service.waiting.WaitingQueryService;

import java.util.ArrayList;
import java.util.List;

@RestController
public class MemberReservationController {

    private final ReservationQueryService reservationQueryService;
    private final WaitingQueryService waitingQueryService;

    public MemberReservationController(ReservationQueryService reservationQueryService, WaitingQueryService waitingQueryService) {
        this.reservationQueryService = reservationQueryService;
        this.waitingQueryService = waitingQueryService;
    }

    @GetMapping("/reservations/me")
    public ResponseEntity<List<ReservationAndWaitingResponseDto>> getMyReservations(@CurrentMember LoginInfo loginInfo) {
        long memberId = loginInfo.id();
        List<ReservationAndWaitingResponseDto> myReservations = reservationQueryService.findMyReservations(memberId);
        List<ReservationAndWaitingResponseDto> myWaiting = waitingQueryService.findMyWaiting(memberId);

        List<ReservationAndWaitingResponseDto> combined = new ArrayList<>();
        combined.addAll(myReservations);
        combined.addAll(myWaiting);

        return ResponseEntity.ok(combined);
    }
}
