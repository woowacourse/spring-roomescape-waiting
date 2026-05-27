package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.WaitingListCreateCommand;
import roomescape.dto.WaitingListResult;
import roomescape.service.WaitingListService;

import java.net.URI;

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
}
