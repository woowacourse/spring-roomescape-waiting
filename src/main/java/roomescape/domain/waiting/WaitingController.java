package roomescape.domain.waiting;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.waiting.dto.MyWaitingsResponse;
import roomescape.domain.waiting.dto.WaitingRequest;
import roomescape.domain.waiting.dto.WaitingResponse;

@Validated
@RestController
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<WaitingResponse> createWaiting(
        @RequestBody @Valid WaitingRequest waitingRequest
    ) {

        WaitingResponse response = waitingService.createWaiting(waitingRequest);
        URI location = URI.create("/reservations/waiting/" + response.id());

        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/reservations/waiting/mine")
    public ResponseEntity<MyWaitingsResponse> getMyWaitings(
        @RequestParam
        @NotBlank(message = "이름은 필수 입력 값입니다.")
        @Size(max = 100, message = "이름은 100자 이하여야 합니다.")
        String name
    ) {
        MyWaitingsResponse response = waitingService.getMyWaitings(name);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/reservations/waiting/{id}")
    public ResponseEntity<Void> deleteReservation(
        @PathVariable Long id, @RequestParam String name
    ) {
        waitingService.deleteWaiting(id, name);
        return ResponseEntity.noContent().build();
    }

}
