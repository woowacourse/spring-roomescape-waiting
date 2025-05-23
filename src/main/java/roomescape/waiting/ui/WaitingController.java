package roomescape.waiting.ui;

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
import roomescape.waiting.application.dto.WaitingIdResponse;
import roomescape.waiting.application.dto.WaitingInfoResponse;

@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/waitings")
    public ResponseEntity<WaitingIdResponse> add(
        @Valid @RequestBody final MemberReservationRequest request,
        final LoginCheckRequest loginCheckRequest
    ) {
        WaitingIdResponse waitingIdResponse = waitingService.addWaiting(request,
            loginCheckRequest.id());
        return new ResponseEntity<>(waitingIdResponse, HttpStatus.CREATED);
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

    @GetMapping("/admin/waitings")
    public ResponseEntity<List<WaitingInfoResponse>> findAllWaitings() {
        List<WaitingInfoResponse> allWaitingInfos = waitingService.getAllWaitingInfos();
        return new ResponseEntity<>(allWaitingInfos, HttpStatus.OK);
    }

}
