package roomescape.web.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
public class MemberReservationWaitingController {

    private final ReservationWaitingService reservationWaitingService;

    public MemberReservationWaitingController(ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingWebResponse> create(
            @Valid @RequestBody ReservationWaitingWebRequest request,
            @Auth LoginMemberInformation loginMember) {

        ReservationWaitingAppRequest appRequest = new ReservationWaitingAppRequest(
                LocalDate.parse(request.date()), request.timeId(), request.themeId(), loginMember.id());
        ReservationWaitingAppResponse appResponse = reservationWaitingService.save(appRequest);

        ReservationWaitingWebResponse webResponse = ReservationWaitingWebResponse.from(appResponse);
        Long id = webResponse.id();

        return ResponseEntity.created(URI.create("/reservations/waiting/" + id)).body(webResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationWaitingService.deleteBy(id);

        return ResponseEntity.noContent().build();
    }
}
