package roomescape.reservation.ui;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.login.application.dto.LoginCheckRequest;
import roomescape.waiting.application.WaitingService;
import roomescape.reservation.application.dto.MemberReservationRequest;
import roomescape.reservation.application.dto.MyReservation;
import roomescape.waiting.application.dto.WaitingResponse;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingResponse> add(
        @Valid @RequestBody final MemberReservationRequest request,
        final LoginCheckRequest loginCheckRequest
    ) {
        WaitingResponse waitingResponse = waitingService.addWaiting(request,
            loginCheckRequest.id());
        return new ResponseEntity<>(waitingResponse, HttpStatus.CREATED);
    }

    @DeleteMapping("/waitings/{waitingId}")
    public ResponseEntity<Void> delete(
        @PathVariable("waitingId") Long id,
        LoginCheckRequest loginCheckRequest
    ) {
        waitingService.cancel(loginCheckRequest.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<MyReservation>> get(LoginCheckRequest loginCheckRequest) {
        List<MyReservation> waitingsFromMember = waitingService.getWaitingsFromMember(
            loginCheckRequest.id());
        return new ResponseEntity<>(waitingsFromMember, HttpStatus.OK);
    }

}
