package roomescape.controller.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import roomescape.controller.dto.ReservationWaitingRequest;
import roomescape.controller.dto.ReservationWaitingResponse;
import roomescape.domain.WaitingWithTurn;
import roomescape.service.ReservationWaitingService;

import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;

@Validated
@RestController
@RequestMapping("/waitings")
public class ReservationWaitingController {

    private final ReservationWaitingService service;

    public ReservationWaitingController(ReservationWaitingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> createReservationWaiting(
            @Valid @RequestBody ReservationWaitingRequest request
    ) {
        WaitingWithTurn waitingWithTurn = service.create(
                request.name(),
                request.date(),
                request.timeId(),
                request.themeId(),
                LocalDateTime.now()
        );
        return ResponseEntity.created(URI.create("/waitings/" + waitingWithTurn.waiting().getId()))
                .body(ReservationWaitingResponse.from(waitingWithTurn));
    }

    @GetMapping
    public ResponseEntity<List<ReservationWaitingResponse>> getReservationWaitingsByName(
            @RequestParam("name") @NotBlank(message = "name은 비어 있을 수 없습니다.") String name
    ) {
        List<ReservationWaitingResponse> reservationWaitings = service.findByName(name).stream()
                .map(ReservationWaitingResponse::from)
                .toList();
        return ResponseEntity.ok(reservationWaitings);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservation(
            @PathVariable @Positive(message = "id는 양수이어야 합니다.") Long id,
            @RequestParam("name") @NotBlank(message = "name은 비어 있을 수 없습니다.") String name
    ) {
        service.delete(id, name, LocalDateTime.now());
        return ResponseEntity.noContent().build();
    }
}
