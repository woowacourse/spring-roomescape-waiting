package roomescape.web.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationService;
import roomescape.service.ReservationWaitingService;
import roomescape.service.request.ReservationAppRequest;
import roomescape.service.response.ReservationAppResponse;
import roomescape.web.auth.Auth;
import roomescape.web.controller.request.LoginMemberInformation;
import roomescape.web.controller.request.MemberReservationWebRequest;
import roomescape.web.controller.response.MemberReservationWebResponse;
import roomescape.web.controller.response.ReservationMineWebResponse;

@RestController
@RequestMapping("/reservations")
public class MemberReservationController {

    private final ReservationService reservationService;
    private final ReservationWaitingService reservationWaitingService;

    public MemberReservationController(ReservationService reservationService,
                                       ReservationWaitingService reservationWaitingService) {
        this.reservationService = reservationService;
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<MemberReservationWebResponse> reserve(
            @Valid @RequestBody MemberReservationWebRequest request,
            @Valid @Auth LoginMemberInformation loginMemberInformation) {

        ReservationAppResponse appResponse = reservationService.save(
                new ReservationAppRequest(request.date(), request.timeId(),
                        request.themeId(), loginMemberInformation.id()));

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

        List<MemberReservationWebResponse> memberReservationWebResponse = reservationService.findAll().stream()
                .map(MemberReservationWebResponse::from)
                .toList();

        return ResponseEntity.ok(memberReservationWebResponse);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<ReservationMineWebResponse>> getMyReservations(
            @Auth LoginMemberInformation loginMember) {
        Long memberId = loginMember.id();

        List<ReservationMineWebResponse> reservationMineWebResponses = reservationService.findByMemberId(
                        memberId)
                .stream()
                .map(ReservationMineWebResponse::from)
                .collect(Collectors.toList());

        reservationMineWebResponses.addAll(reservationWaitingService.findAllByMemberId(memberId).stream()
                .map(ReservationMineWebResponse::from)
                .toList());

        return ResponseEntity.ok(reservationMineWebResponses);
    }

}
