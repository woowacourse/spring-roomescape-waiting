package roomescape.web.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import roomescape.domain.Waiting;
import roomescape.service.WaitingCommandService;
import roomescape.service.WaitingQueryService;
import roomescape.web.dto.request.WaitingRequest;
import roomescape.web.dto.response.WaitingResponse;

import java.net.URI;


@RestController
@RequestMapping("/reservations/waitings")
@RequiredArgsConstructor
public class WaitingController {

    private final WaitingCommandService waitingCommandService;
    private final WaitingQueryService waitingQueryService;


    @PostMapping
    ResponseEntity<WaitingResponse> createWaiting(@Valid @RequestBody WaitingRequest request) {
        Waiting waiting = waitingCommandService.create(WaitingRequest.toCommand(request));

        WaitingResponse waitingResponse = WaitingResponse.from(waiting);

        Long savedId = waitingResponse.id();

        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(savedId)
                .toUri();

        return ResponseEntity.created(location).body(waitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelWaiting(@PathVariable("id") long id, @RequestParam String name) {
        waitingCommandService.cancel(id, name);
        return ResponseEntity.noContent().build();
    }
}
