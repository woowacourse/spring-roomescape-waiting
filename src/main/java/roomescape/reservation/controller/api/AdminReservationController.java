package roomescape.reservation.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import roomescape.config.annotation.AuthMember;
import roomescape.member.entity.Member;
import roomescape.reservation.controller.dto.request.ReservationRequest;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.waiting.controller.dto.response.WaitingWithRankResponse;
import roomescape.waiting.entity.WaitingWithRank;
import roomescape.reservation.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    private AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
        @AuthMember Member member,
        @RequestBody @Valid ReservationRequest request
    ) {
        return ReservationResponse.from(reservationService.addReservation(member,request));
    }

    @GetMapping("/waiting")
    public List<WaitingWithRankResponse> readAllReservationWaiting() {
        List<WaitingWithRank> allWaiting = reservationService.findAllWaiting();
        return allWaiting.stream()
                .map(WaitingWithRankResponse::from)
                .toList();
    }
}
