package roomescape.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.request.WaitingRequest;
import roomescape.controller.dto.response.WaitingListResponse;
import roomescape.controller.dto.response.WaitingResponse;
import roomescape.service.WaitingService;
import roomescape.service.dto.WaitingResult;

import java.util.List;

@RestController
@RequestMapping("/waitings")
public class WaitingController {

    private final WaitingService waitingService;

    public WaitingController(WaitingService waitingService) {
        this.waitingService = waitingService;
    }

    @GetMapping
    public ResponseEntity<WaitingListResponse> findMyWaitList(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        List<WaitingResult> userWaitList = waitingService.findUserWaitList(name, page, size);
        WaitingListResponse response = WaitingListResponse.from(userWaitList);
        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

    @PostMapping
    public ResponseEntity<WaitingResponse> createWaiting(@Valid @RequestBody WaitingRequest waitingRequest) {
        WaitingResult waitingResult = waitingService.createWaiting(waitingRequest.name(), waitingRequest.date(), waitingRequest.timeId(), waitingRequest.themeId());
        WaitingResponse response = WaitingResponse.from(waitingResult);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping(value = "/{id}", params = "name")
    public ResponseEntity<Void> deleteWaiting(@PathVariable Long id, @RequestParam String name) {
        waitingService.deleteWaiting(id, name);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
