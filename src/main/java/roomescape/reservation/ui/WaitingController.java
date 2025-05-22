package roomescape.reservation.ui;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.login.application.dto.LoginCheckRequest;
import roomescape.reservation.application.WaitingService;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.WaitingResponse;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/wait")
    public ResponseEntity<WaitingResponse> add(
        @Valid @RequestBody final MemberReservationRequest request,
        final LoginCheckRequest loginCheckRequest
    ) {
        System.out.println("데이트 잘 찍히나 " + request.date());

        WaitingResponse waitingResponse = waitingService.addWaiting(request,
            loginCheckRequest.id());

        System.out.println("웨이팅 아이디 = " + waitingResponse.waitingId());
        return new ResponseEntity<>(waitingResponse, HttpStatus.CREATED);
    }
}
