package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.WaitingListDeleteRequest;
import roomescape.service.WaitingListService;
import roomescape.service.dto.command.WaitingListCreateCommand;
import roomescape.service.dto.command.WaitingListDeleteCommand;
import roomescape.service.dto.result.WaitingListResult;

import java.net.URI;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RequiredArgsConstructor
@RequestMapping("/waiting-list")
@RestController
public class WaitingListController {

    private final WaitingListService waitingListService;

    @GetMapping
    public ResponseEntity<List<WaitingListResult>> getWaitingListsByName(@RequestParam final String name) {
        final List<WaitingListResult> results = waitingListService.getWaitingListByName(name);
        return ResponseEntity.ok().body(results);
    }


    @PostMapping
    public ResponseEntity<WaitingListResult> create(@RequestBody @Valid final WaitingListCreateCommand createCommand) {
        final WaitingListResult result = waitingListService.create(createCommand, LocalDate.now(), LocalTime.now());
        return ResponseEntity.created(URI.create("/waiting-list/" + result.id()))
                .body(result);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<WaitingListResult> delete(
            @PathVariable final Long id,
            @RequestBody final WaitingListDeleteRequest waitingListDeleteRequest) {
        final WaitingListDeleteCommand deleteCommand = new WaitingListDeleteCommand(id, waitingListDeleteRequest.name());
        waitingListService.delete(deleteCommand, LocalDate.now(), LocalTime.now());
        return ResponseEntity.noContent()
                .build();
    }
}
