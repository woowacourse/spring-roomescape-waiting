package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import roomescape.controller.dto.ReservationDeleteRequest;
import roomescape.controller.dto.ReservationModifyRequest;
import roomescape.service.ReservationService;
import roomescape.service.dto.command.ReservationCreateCommand;
import roomescape.service.dto.command.ReservationDeleteCommand;
import roomescape.service.dto.command.ReservationModifyCommand;
import roomescape.service.dto.result.AvailableDateResult;
import roomescape.service.dto.result.ReservationResult;
import roomescape.service.dto.result.ReservationTimeStatusResult;

import java.net.URI;
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

    @PostMapping
    public ResponseEntity<ReservationResult> create(
            @Valid @RequestBody final ReservationCreateCommand request
    ) {
        final ReservationResult result = reservationService.create(request);
        return ResponseEntity.created(URI.create("/reservations/" + result.id()))
                .body(result);
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
                reservationModifyRequest.timeId()
        );
        final ReservationResult result = reservationService.modify(reservationModifyCommand);
        return ResponseEntity.ok(result);
    }

    @DeleteMapping("/{reservation-id}")
    public ResponseEntity<Void> deleteByName(
            @PathVariable("reservation-id") final Long reservationId,
            @RequestBody final ReservationDeleteRequest reservationDeleteRequest
            ) {
        final ReservationDeleteCommand deleteCommand = new ReservationDeleteCommand(reservationId, reservationDeleteRequest.name());
        reservationService.deleteWithValidation(deleteCommand);
        return ResponseEntity.noContent().build();
    }
}
