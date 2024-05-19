package roomescape.web.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationService;
import roomescape.service.request.ReservationAppRequest;
import roomescape.service.response.ReservationAppResponse;
import roomescape.web.auth.Auth;
import roomescape.web.controller.request.LoginMember;
import roomescape.web.controller.request.MemberReservationWebRequest;
import roomescape.web.controller.response.MemberReservationWebResponse;
import roomescape.web.controller.response.ReservationMineWebResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class MemberReservationController {

    private final ReservationService reservationService;

    public MemberReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<MemberReservationWebResponse> reserve(@Valid @RequestBody MemberReservationWebRequest request,
                                                                @Valid @Auth
                                                                LoginMember loginMember) {
        ReservationAppResponse appResponse = reservationService.save(
                new ReservationAppRequest(request.date(), request.timeId(),
                        request.themeId(), loginMember.id()));

        Long id = appResponse.id();
        MemberReservationWebResponse memberReservationWebResponse = MemberReservationWebResponse.from(appResponse);

        return ResponseEntity.created(URI.create("/reservations/" + id))
                .body(memberReservationWebResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBy(@PathVariable Long id) {
        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<MemberReservationWebResponse>> getReservations() {
        List<ReservationAppResponse> appResponses = reservationService.findAll();
        List<MemberReservationWebResponse> memberReservationWebResponse = appResponses.stream()
                .map(MemberReservationWebResponse::from)
                .toList();

        return ResponseEntity.ok(memberReservationWebResponse);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationMineWebResponse>> getMyReservations(@Auth LoginMember loginMember) {
        List<ReservationMineWebResponse> reservationMineWebResponses = reservationService.findByMemberId(loginMember.id())
                .stream()
                .map(ReservationMineWebResponse::new)
                .toList();

        return ResponseEntity.ok(reservationMineWebResponses);
    }

}
