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
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationResult;
import roomescape.reservation.application.dto.ReservationSearchCondition;
import roomescape.reservation.application.service.WaitingCommandService;
import roomescape.reservation.application.service.WaitingQueryService;
import roomescape.reservation.presentation.dto.ReservationCreateRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;

@RequiredArgsConstructor
@RequestMapping("/waitings")
@RestController
public class WaitingController {

    private final WaitingCommandService waitingCommandService;
    private final WaitingQueryService waitingQueryService;

    @PostMapping
    public ResponseEntity<ReservationResponse> save(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationCreateCommand createCommand = request.toCommand(LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(waitingCommandService.save(createCommand)));
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findByName(
            @RequestParam String username
    ) {
        List<ReservationResult> results = waitingQueryService.findByName(new ReservationSearchCondition(username));

        List<ReservationResponse> responses = results.stream()
                .map(ReservationResponse::from)
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
