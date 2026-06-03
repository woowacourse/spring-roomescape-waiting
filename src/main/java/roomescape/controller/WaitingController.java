package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.request.WaitingRequest;
import roomescape.dto.response.WaitingResponse;
import roomescape.dto.response.WaitingWithRankResponse;
import roomescape.service.WaitingService;

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
        WaitingResponse response = waitingService.create(request, LocalDateTime.now());
        return ResponseEntity.created(URI.create(LOCATION_DEFAULT_VALUE + response.id()))
                .body(response);
    }

    @GetMapping(params = {"name"})
    public ResponseEntity<List<WaitingWithRankResponse>> readWaitingsWithQuery(@RequestParam(value = "name") String name) {
        List<WaitingWithRankResponse> responses = waitingService.getWaitingsByName(name);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{waitingId}")
    public ResponseEntity<Void> delete(@PathVariable("waitingId") long waitingId, @RequestParam("name") String name) {
        waitingService.delete(waitingId, name);
        return ResponseEntity.noContent()
                .build();
    }
}
