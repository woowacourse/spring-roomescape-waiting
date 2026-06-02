package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.ReservationModifyRequest;
import roomescape.service.ReservationService;
import roomescape.dto.ReservationCreateCommand;
import roomescape.dto.ReservationModifyCommand;
import roomescape.dto.AvailableDateResult;
import roomescape.dto.ReservationResult;
import roomescape.dto.ReservationTimeStatusResult;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping("/available-dates")
    public ResponseEntity<AvailableDateResult> getAvailableDates() {
        final AvailableDateResult results = reservationService.getReservationOptions();
        return ResponseEntity.ok(results);
    }

    @GetMapping(path = "/available-times")
    public ResponseEntity<List<ReservationTimeStatusResult>> getReservationTimeStatuses(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") Long themeId
    ) {
        final List<ReservationTimeStatusResult> results = reservationService.getReservationTimeStatuses(date, themeId);
        return ResponseEntity.ok(results);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResult>> getReservationsByName(@RequestParam("name") String name) {
        final List<ReservationResult> results = reservationService.getReservationsByName(name);
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<ReservationResult> create(
            @Valid @RequestBody ReservationCreateCommand request
    ) {
        final ReservationResult result = reservationService.create(request);
        return ResponseEntity.created(URI.create("/reservations/" + result.id()))
                .body(result);
    }

    @DeleteMapping("/{reservation-id}")
    public ResponseEntity<Void> deleteByName(
            @PathVariable("reservation-id") Long reservationId,
            @RequestParam("name") String name
    ) {
        reservationService.deleteWithValidation(reservationId, name);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{reservation-id}")
    public ResponseEntity<ReservationResult> modify(
            @PathVariable("reservation-id") Long reservationId,
            @Valid @RequestBody ReservationModifyRequest reservationModifyRequest
    ) {
        final ReservationModifyCommand reservationModifyCommand = new ReservationModifyCommand(
                reservationId,
                reservationModifyRequest.name(),
                reservationModifyRequest.date(),
                reservationModifyRequest.timeId()
        );
        final ReservationResult result = reservationService.modify(reservationModifyCommand);
        return ResponseEntity.ok(result);
    }
}
