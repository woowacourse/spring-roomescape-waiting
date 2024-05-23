package roomescape.presentation.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.WaitingService;
import roomescape.application.reservation.dto.request.ReservationRequest;
import roomescape.application.reservation.dto.response.ReservationResponse;
import roomescape.presentation.auth.LoginMemberId;
import roomescape.presentation.auth.PermissionRequired;

@RestController
@RequestMapping("/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createWaiting(@LoginMemberId long memberId,
                                                             @RequestBody @Valid ReservationRequest request) {
        ReservationResponse response = waitingService.create(request.withMemberId(memberId));
        URI location = URI.create("/reservations/waiting/" + response.id());
        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(@LoginMemberId long memberId, @PathVariable long id) {
        waitingService.deleteByIdWhenAuthorization(memberId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    @PermissionRequired
    public ResponseEntity<List<ReservationResponse>> findAllWaiting() {
        List<ReservationResponse> responses = waitingService.findAll();
        return ResponseEntity.ok(responses);
    }
}
