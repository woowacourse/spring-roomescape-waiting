package roomescape.controller.api;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.dto.auth.CurrentMember;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.reservation.MyReservationResponseDto;
import roomescape.service.reservation.ReservationQueryService;
import roomescape.service.waiting.WaittingQueryService;

@Controller
public class MemberReservationController {

    private final ReservationQueryService reservationQueryService;
    private final WaittingQueryService waitingQueryService;

    public MemberReservationController(ReservationQueryService reservationQueryService, WaittingQueryService waitingQueryService) {
        this.reservationQueryService = reservationQueryService;
        this.waitingQueryService = waitingQueryService;
    }

    @GetMapping("/reservations/me")
    public ResponseEntity<List<MyReservationResponseDto>> getMyReservations(@CurrentMember LoginInfo loginInfo) {
        long memberId = loginInfo.id();
        List<MyReservationResponseDto> myReservations = reservationQueryService.findMyReservations(memberId);
        List<MyReservationResponseDto> myWaiting = waitingQueryService.findMyWaiting(memberId);

        List<MyReservationResponseDto> combined = new ArrayList<>();
        combined.addAll(myReservations);
        combined.addAll(myWaiting);

        return ResponseEntity.ok(combined);
    }
}
