package roomescape.web.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationWaitingService;
import roomescape.service.request.WaitingAppRequest;
import roomescape.service.response.ReservationWaitingAppResponse;
import roomescape.web.auth.Auth;
import roomescape.web.controller.request.LoginMember;
import roomescape.web.controller.request.MemberWaitingWebRequest;
import roomescape.web.controller.response.ReservationWaitingWebResponse;
import roomescape.web.controller.response.ReservationWaitingWithRankWebResponse;

import java.net.URI;
import java.util.List;

@Controller
@RequestMapping("/waitings")
public class MemberReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public MemberReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingWebResponse> save(@Valid @RequestBody MemberWaitingWebRequest request, @Valid @Auth LoginMember loginMember) {

        ReservationWaitingAppResponse waitingAppResponse = reservationWaitingService.save(
                new WaitingAppRequest(request.date(), request.timeId(),
                        request.themeId(), loginMember.id()));
        ReservationWaitingWebResponse waitingWebResponse = new ReservationWaitingWebResponse(waitingAppResponse);

        return ResponseEntity.created(URI.create("/waitings/" + waitingWebResponse.id()))
                .body(waitingWebResponse);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationWaitingWithRankWebResponse>> findMyWaitingWithRank(@Valid @Auth LoginMember loginMember) {
        Long memberId = loginMember.id();
        List<ReservationWaitingWithRankWebResponse> waitingWithRankWebResponses = reservationWaitingService.findWaitingWithRankByMemberId(memberId)
                .stream()
                .map(ReservationWaitingWithRankWebResponse::new)
                .toList();

        return ResponseEntity.ok(waitingWithRankWebResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@Valid @Auth LoginMember loginMember, @PathVariable Long id) {
        reservationWaitingService.deleteMemberWaiting(loginMember.id(), id);

        return ResponseEntity.noContent().build();
    }
}
