package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.Login;
import roomescape.member.dto.LoginMemberInToken;
import roomescape.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.WaitingCreateRequest;
import roomescape.reservation.dto.WaitingResponse;
import roomescape.reservation.service.WaitingService;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(final WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waiting")
    public ResponseEntity<WaitingResponse> createWaiting(
            @Valid @RequestBody WaitingCreateRequest waitingCreateRequest,
            @Login LoginMemberInToken loginMemberInToken
    ) {
        Long id = waitingService.save(waitingCreateRequest, loginMemberInToken);
        WaitingResponse waitingResponse = waitingService.findById(id);

        return ResponseEntity.created(URI.create("/waiting/" + id)).body(waitingResponse);
    }
}
