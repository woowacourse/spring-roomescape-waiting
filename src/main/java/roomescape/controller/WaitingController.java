package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.dto.response.WaitingWithRankResponse;
import roomescape.service.WaitingService;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private static final String LOCATION_DEFAULT_VALUE = "/waitings/";

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> create(@Valid @RequestBody WaitingRequest request) {
        WaitingResponse response = waitingService.create(request);
        return ResponseEntity.created(URI.create(LOCATION_DEFAULT_VALUE + response.id()))
                .body(response);
    }

    @GetMapping(params = {"name"})
    public ResponseEntity<List<WaitingWithRankResponse>> readWaitingsWithQuery(@RequestParam(value = "name") String name) {
        List<WaitingWithRankResponse> responses = waitingService.getWaitingsByName(name);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{waitingId}")
    public ResponseEntity<Void> delete(@PathVariable long waitingId, @RequestParam String name) {
        waitingService.delete(waitingId, name);
        return ResponseEntity.noContent()
                .build();
    }
}
