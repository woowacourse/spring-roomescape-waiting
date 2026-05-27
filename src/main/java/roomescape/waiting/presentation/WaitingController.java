package roomescape.waiting.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.waiting.application.WaitingService;
import roomescape.waiting.application.dto.WaitingCreateCommand;
import roomescape.waiting.presentation.dto.request.WaitingCreateRequest;
import roomescape.waiting.presentation.dto.response.WaitingResponse;

@RestController
@RequestMapping("/waiting")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> saveWaiting(
            @RequestBody @Valid WaitingCreateRequest request
    ) {
        WaitingCreateCommand command = new WaitingCreateCommand(
                request.name(),
                request.date(),
                request.timeId(),
                request.themeId()
        );
        WaitingResponse response = WaitingResponse.from(waitingService.save(command));
        return ResponseEntity.created(URI.create("/waiting/" + response.id()))
                .body(response);
    }
}
