package roomescape.reservation.presentation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.dto.WaitingCreateCommand;
import roomescape.reservation.application.dto.WaitingResult;
import roomescape.reservation.application.service.WaitingCommandService;
import roomescape.reservation.application.service.WaitingQueryService;
import roomescape.reservation.presentation.dto.WaitingCreateRequest;
import roomescape.reservation.presentation.dto.WaitingResponse;

@RequiredArgsConstructor
@Validated
@RequestMapping("/waitings")
@RestController
public class WaitingController {

    private final WaitingCommandService waitingCommandService;
    private final WaitingQueryService waitingQueryService;

    @PostMapping
    public ResponseEntity<WaitingResponse> save(
            @Valid @RequestBody WaitingCreateRequest request
    ) {
        WaitingCreateCommand createCommand = request.toCommand(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(WaitingResponse.from(waitingCommandService.save(createCommand)));
    }

    @GetMapping
    public ResponseEntity<List<WaitingResponse>> findByName(
            @NotBlank(message = "이름은 비어있을 수 없습니다.")
            @RequestParam String username
    ) {
        List<WaitingResult> results = waitingQueryService.findByName(username);

        List<WaitingResponse> responses = results.stream()
                .map(WaitingResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWaiting(
            @PathVariable Long id
    ) {
        waitingCommandService.delete(id, LocalDateTime.now());
        return ResponseEntity.noContent().build();
    }
}
