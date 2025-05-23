package roomescape.controller.reservation;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reserveticket.ReserveTicket;
import roomescape.domain.waiting.WaitingWithRank;
import roomescape.dto.reservationmember.MyReservationMemberResponseDto;
import roomescape.infrastructure.auth.intercept.AuthenticationPrincipal;
import roomescape.infrastructure.auth.member.UserInfo;
import roomescape.service.reserveticket.ReserveTicketService;
import roomescape.service.waiting.WaitingService;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/reservations/mine")
public class MyReservationController {

    private final ReserveTicketService reserveTicketService;
    private final WaitingService waitingService;

    public MyReservationController(ReserveTicketService reserveTicketService, WaitingService waitingService) {
        this.reserveTicketService = reserveTicketService;
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<List<MyReservationMemberResponseDto>> myReservations(@AuthenticationPrincipal UserInfo userInfo) {
        List<ReserveTicket> reserveTickets = reserveTicketService.memberReservations(userInfo.id());
        List<MyReservationMemberResponseDto> reservationDtos = reserveTickets.stream()
                .map(MyReservationMemberResponseDto::from)
                .collect(Collectors.toList());
        List<WaitingWithRank> waitingsWithRank = waitingService.getWaitingsWithRankByMemberId(userInfo.id());
        for (WaitingWithRank waitingWithRank : waitingsWithRank) {
            reservationDtos.add(MyReservationMemberResponseDto.from(waitingWithRank));
        }
        return ResponseEntity.ok(reservationDtos);
    }
}
