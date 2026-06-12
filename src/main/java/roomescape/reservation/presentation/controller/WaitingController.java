package roomescape.reservation.presentation.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.dto.ReservationApplicationCreateCommand;
import roomescape.reservation.application.dto.ReservationApplicationResult;
import roomescape.reservation.application.dto.ReservationApplicationSearchCondition;
import roomescape.reservation.application.service.WaitingCommandService;
import roomescape.reservation.application.service.WaitingQueryService;
import roomescape.reservation.presentation.dto.ReservationApplicationCreateRequest;
import roomescape.reservation.presentation.dto.ReservationApplicationResponse;

@RequiredArgsConstructor
@RequestMapping("/waitings")
@RestController
public class WaitingController {

    private final WaitingCommandService waitingCommandService;
    private final WaitingQueryService waitingQueryService;

    @PostMapping
    public ResponseEntity<ReservationApplicationResponse> save(
            @Valid @RequestBody ReservationApplicationCreateRequest request
    ) {
        ReservationApplicationCreateCommand createCommand = request.toCommand(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationApplicationResponse.from(waitingCommandService.save(createCommand)));
    }

    @GetMapping
    public ResponseEntity<List<ReservationApplicationResponse>> findByName(
            @RequestParam String username
    ) {
        List<ReservationApplicationResult> results = waitingQueryService.findByName(
                new ReservationApplicationSearchCondition(username));

        List<ReservationApplicationResponse> responses = results.stream()
                .map(ReservationApplicationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelWaiting(
            @PathVariable Long id
    ) {
        waitingCommandService.cancel(id, LocalDateTime.now());
        return ResponseEntity.noContent().build();
    }
}
