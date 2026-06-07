package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.WaitingListCreateCommand;
import roomescape.dto.WaitingListDeleteCommand;
import roomescape.dto.WaitingListDeleteRequest;
import roomescape.dto.WaitingListResult;
import roomescape.service.WaitingListService;

import java.net.URI;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/waiting-list")
@RestController
public class WaitingListController {

    private final WaitingListService waitingListService;

    @PostMapping
    public ResponseEntity<WaitingListResult> create(@RequestBody @Valid WaitingListCreateCommand createCommand) {
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
