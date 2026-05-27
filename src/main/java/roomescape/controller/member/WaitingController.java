package roomescape.controller.member;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.controller.dto.request.WaitingRequest;
import roomescape.controller.dto.response.WaitingResponse;
import roomescape.service.WaitingService;
import roomescape.service.dto.command.WaitingCommand;
import roomescape.service.dto.result.WaitingResult;

@RestController
@RequestMapping("/waitings")
public class WaitingController {
    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createWaiting(@Valid @RequestBody WaitingRequest request) {
        WaitingResult result = waitingService.save(WaitingCommand.from(request));
        WaitingResponse response = WaitingResponse.from(result);

        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.id())
                .toUri();

        return ResponseEntity.created(location).body(response);
    }

    @DeleteMapping(value = {"/{id}"})
    public ResponseEntity<WaitingResponse> deleteWaiting(@PathVariable long id, @RequestParam String name) {
        waitingService.delete(id, name);
        return ResponseEntity.noContent().build();
    }

}