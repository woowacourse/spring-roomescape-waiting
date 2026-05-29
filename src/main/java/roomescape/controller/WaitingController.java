package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.Waiting;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.service.WaitingCommandService;

import java.net.URI;


@RestController
@RequestMapping("/reservations/waitings")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingCommandService waitingCommandService;

    @PostMapping
    ResponseEntity<WaitingResponse> createWaiting(@Valid @RequestBody WaitingRequest request) {
        Waiting waiting = waitingCommandService.create(request.name(), request.date(), request.timeId(), request.themeId());

        WaitingResponse waitingResponse = WaitingResponse.from(waiting);

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(waitingResponse.id())
                .toUri();

        return ResponseEntity.created(location).body(waitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelWaiting(@PathVariable long id, @RequestParam String name) {
        waitingCommandService.cancel(id, name);
        return ResponseEntity.noContent().build();
    }
}
