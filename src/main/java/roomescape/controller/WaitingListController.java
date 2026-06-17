package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.request.WaitingListCreateRequest;
import roomescape.dto.command.WaitingListDeleteCommand;
import roomescape.dto.request.WaitingListDeleteRequest;
import roomescape.dto.response.WaitingListResult;
import roomescape.service.WaitingListService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/waiting-list")
@RestController
public class WaitingListController {

    private final WaitingListService waitingListService;

    @PostMapping
    public ResponseEntity<WaitingListResult> create(@RequestBody @Valid WaitingListCreateRequest createCommand) {
        final WaitingListResult result = waitingListService.create(createCommand);
        return ResponseEntity.created(URI.create("/waiting-list/" + result.id()))
                .body(result);
    }

    @GetMapping
    public ResponseEntity<List<WaitingListResult>> getWaitingListsByName(@RequestParam final String name) {
        final List<WaitingListResult> results = waitingListService.getWaitingListByName(name);
        return ResponseEntity.ok().body(results);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WaitingListResult> delete(
            @PathVariable final Long id,
            @RequestBody final WaitingListDeleteRequest waitingListDeleteRequest) {
        final WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(id, waitingListDeleteRequest.name());
        waitingListService.delete(deleteCommand);
        return ResponseEntity.noContent()
                .build();
    }
}
