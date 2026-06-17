package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.request.ReservationModifyRequest;
import roomescape.service.ReservationService;
import roomescape.dto.request.ReservationCreateRequest;
import roomescape.dto.command.ReservationModifyCommand;
import roomescape.dto.response.AvailableDateResult;
import roomescape.dto.response.ReservationResult;
import roomescape.dto.response.ReservationTimeStatusResult;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ReservationResult> create(
            @Valid @RequestBody ReservationCreateRequest request
    ) {
        final ReservationResult result = reservationService.create(request);
        return ResponseEntity.created(URI.create("/reservations/" + result.id()))
                .body(result);
    }

    @GetMapping("/available-dates")
    public ResponseEntity<AvailableDateResult> getAvailableDates() {
        final AvailableDateResult results = reservationService.getReservationOptions();
        return ResponseEntity.ok(results);
    }

    @GetMapping(path = "/available-times")
    public ResponseEntity<List<ReservationTimeStatusResult>> getReservationTimeStatuses(
            @RequestParam("date") final LocalDate date,
            @RequestParam("themeId") final Long themeId
    ) {
        final List<ReservationTimeStatusResult> results = reservationService.getReservationTimeStatuses(date, themeId);
        return ResponseEntity.ok(results);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResult>> getReservationsByName(@RequestParam("name") final String name) {
        final List<ReservationResult> results = reservationService.getReservationsByName(name);
        return ResponseEntity.ok(results);
    }

    @PatchMapping("/{reservation-id}")
    public ResponseEntity<ReservationResult> modify(
            @PathVariable("reservation-id") final Long reservationId,
            @Valid @RequestBody final ReservationModifyRequest reservationModifyRequest
    ) {
        final ReservationModifyCommand reservationModifyCommand = new ReservationModifyCommand(
                reservationId,
                reservationModifyRequest.name(),
                reservationModifyRequest.date(),
                reservationModifyRequest.timeId(),
                reservationModifyRequest.themeId()
        );
        final ReservationResult result = reservationService.modify(reservationModifyCommand);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{reservation-id}")
    public ResponseEntity<Void> deleteByName(
            @PathVariable("reservation-id") final Long reservationId,
            @RequestParam("name") final String name
    ) {
        reservationService.deleteWithValidation(reservationId, name);
        return ResponseEntity.noContent().build();
    }
}
