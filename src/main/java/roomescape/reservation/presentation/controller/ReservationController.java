package roomescape.reservation.presentation.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.presentation.dto.ReservationCreateRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.ReservationUpdateRequest;

@RequiredArgsConstructor
@RequestMapping("/reservations")
@RestController
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll(@RequestParam String name) {
        List<ReservationResponse> responses = reservationService.findAllByName(name).stream()
                .map(ReservationResponse::from)
                .toList();

        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> save(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        ReservationCreateCommand createCommand = request.toCommand();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(reservationService.save(createCommand, LocalDateTime.now())));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        ReservationUpdateCommand command = request.toCommand(id);

        return ResponseEntity.ok(
                ReservationResponse.from(reservationService.update(command, LocalDateTime.now()))
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        int deletedCount = reservationService.delete(id, name, LocalDateTime.now());

        if (deletedCount == 0) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.noContent().build();
    }
}
