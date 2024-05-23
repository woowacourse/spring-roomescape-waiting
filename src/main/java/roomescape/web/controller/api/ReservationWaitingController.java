package roomescape.web.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationWaitingService;
import roomescape.service.request.ReservationWaitingAppRequest;
import roomescape.service.response.ReservationWaitingAppResponse;
import roomescape.web.auth.Auth;
import roomescape.web.controller.request.LoginMemberInformation;
import roomescape.web.controller.request.ReservationWaitingWebRequest;
import roomescape.web.controller.response.ReservationWaitingWebResponse;

@RestController
@RequestMapping("/reservations/waiting")
public class ReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingWebResponse> create(
            @Valid @RequestBody ReservationWaitingWebRequest request,
            @Auth LoginMemberInformation loginMember) {

        ReservationWaitingAppRequest appRequest = new ReservationWaitingAppRequest(
                LocalDate.parse(request.date()), request.timeId(), request.themeId(), loginMember.id());
        ReservationWaitingAppResponse appResponse = reservationWaitingService.save(appRequest);

        Long id = appResponse.id();
        return ResponseEntity.created(URI.create("/reservations/waiting/" + id)).body(
                ReservationWaitingWebResponse.from(appResponse));
    }
}
