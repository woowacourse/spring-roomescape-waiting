package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.request.ReservationRequest;
import roomescape.controller.response.WaitingResponse;
import roomescape.model.Waiting;
import roomescape.model.member.LoginMember;
import roomescape.service.WaitingService;
import roomescape.service.dto.ReservationDto;

import java.net.URI;

@RestController
@RequestMapping("/reservations/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> addWaiting(@Valid @RequestBody ReservationRequest request, LoginMember member) {
        ReservationDto reservationDto = ReservationDto.of(request, member);
        Waiting waiting = waitingService.saveWaiting(reservationDto);
        WaitingResponse response = WaitingResponse.from(waiting); // TODO: essential?
        return ResponseEntity
                .created(URI.create("/reservations/waiting/" + waiting.getId()))
                .body(response);
    }
}
