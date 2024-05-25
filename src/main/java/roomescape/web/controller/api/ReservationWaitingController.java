package roomescape.web.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationWaitingService;
import roomescape.service.request.ReservationWaitingSaveDto;
import roomescape.service.response.ReservationWaitingDto;
import roomescape.web.auth.Auth;
import roomescape.web.controller.request.LoginMember;
import roomescape.web.controller.request.ReservationWaitingRequest;
import roomescape.web.controller.response.ReservationWaitingResponse;
import roomescape.web.controller.response.ReservationWaitingWithRankResponse;

import java.net.URI;
import java.util.List;

@Controller
@RequestMapping("/reservation-waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> save(@Valid @RequestBody ReservationWaitingRequest request, @Valid @Auth LoginMember loginMember) {

        ReservationWaitingDto waitingAppResponse = reservationWaitingService.save(
                new ReservationWaitingSaveDto(request.date(), request.timeId(),
                        request.themeId(), loginMember.id()));
        ReservationWaitingResponse waitingWebResponse = new ReservationWaitingResponse(waitingAppResponse);

        return ResponseEntity.created(URI.create("/reservation-waitings/" + waitingWebResponse.id()))
                .body(waitingWebResponse);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationWaitingWithRankResponse>> findMyWaitingWithRank(@Valid @Auth LoginMember loginMember) {
        Long memberId = loginMember.id();
        List<ReservationWaitingWithRankResponse> waitingWithRankWebResponses = reservationWaitingService.findWaitingWithRankByMemberId(memberId)
                .stream()
                .map(ReservationWaitingWithRankResponse::new)
                .toList();

        return ResponseEntity.ok(waitingWithRankWebResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@Valid @Auth LoginMember loginMember, @PathVariable Long id) {
        reservationWaitingService.deleteMemberWaiting(loginMember.id(), id);

        return ResponseEntity.noContent().build();
    }
}
