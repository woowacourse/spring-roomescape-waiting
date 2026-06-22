package roomescape.presentation.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
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
import roomescape.application.ReservationTimeApplicationService;
import roomescape.domain.ReservationTime;
import roomescape.domain.projection.ReservationTimeAvailability;
import roomescape.presentation.dto.ReservationTimeAvailabilityResponse;
import roomescape.presentation.dto.ReservationTimeAvailabilityResponses;
import roomescape.presentation.dto.ReservationTimeRequest;
import roomescape.presentation.dto.ReservationTimeResponse;
import roomescape.presentation.dto.ReservationTimeResponses;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeApplicationService reservationTimeApplicationService;

    public ReservationTimeController(
            ReservationTimeApplicationService reservationTimeApplicationService
    ) {
        this.reservationTimeApplicationService = reservationTimeApplicationService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> add(
            @RequestBody @Valid ReservationTimeRequest request
    ) {
        ReservationTime reservationTime = reservationTimeApplicationService.save(request);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationTimeResponse.from(reservationTime));
    }

    @GetMapping
    public ResponseEntity<ReservationTimeResponses> search() {
        List<ReservationTime> reservationTimes = reservationTimeApplicationService.findAll();

        return ResponseEntity.ok()
                .body(ReservationTimeResponses.from(reservationTimes));
    }

    @GetMapping("/availability")
    public ResponseEntity<ReservationTimeAvailabilityResponses> searchAvailableReservationTime(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        List<ReservationTimeAvailability> availabilities
                = reservationTimeApplicationService.findWithAvailability(date, themeId);
        List<ReservationTimeAvailabilityResponse> responses = availabilities.stream()
                .map(ReservationTimeAvailabilityResponse::from)
                .toList();

        return ResponseEntity.ok()
                .body(ReservationTimeAvailabilityResponses.of(responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationTimeApplicationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
