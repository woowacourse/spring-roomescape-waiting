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
import roomescape.reservation.application.dto.BookingCreateCommand;
import roomescape.reservation.application.dto.ReservationUpdateCommand;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.application.service.WaitingService;
import roomescape.reservation.presentation.dto.ReservationCreateRequest;
import roomescape.reservation.presentation.dto.ReservationResponse;
import roomescape.reservation.presentation.dto.ReservationUpdateRequest;

@RequiredArgsConstructor
@RequestMapping("/reservations")
@RestController
public class ReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll(@RequestParam String name) {
        return ResponseEntity.ok(reservationService.findAllByName(name));
    }

    @PostMapping
    public ResponseEntity<Void> save(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        BookingCreateCommand createCommand = request.toCommand();
        waitingService.save(createCommand, LocalDateTime.now());

        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ReservationResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ReservationUpdateRequest request
    ) {
        ReservationUpdateCommand command = request.toCommand(id);

        return ResponseEntity.ok(reservationService.update(command, LocalDateTime.now()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @RequestParam String name
    ) {
        reservationService.delete(id, name, LocalDateTime.now());
        return ResponseEntity.noContent().build();
    }
}
