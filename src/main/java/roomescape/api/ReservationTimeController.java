package roomescape.api;

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
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.dto.ReservationTimeResponses;
import roomescape.dto.TimeWithStatusResponse;
import roomescape.dto.TimeWithStatusResponses;
import roomescape.projection.ReservationTimeAvailability;
import roomescape.service.ReservationTimeCommandService;
import roomescape.service.ReservationTimeQueryService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeCommandService reservationTimeCommandService;
    private final ReservationTimeQueryService reservationTimeQueryService;

    public ReservationTimeController(
            ReservationTimeCommandService reservationTimeCommandService,
            ReservationTimeQueryService reservationTimeQueryService
    ) {
        this.reservationTimeCommandService = reservationTimeCommandService;
        this.reservationTimeQueryService = reservationTimeQueryService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> add(
            @RequestBody @Valid ReservationTimeRequest request
    ) {
        ReservationTime time = new ReservationTime(request.startAt());
        ReservationTime reservationTime = reservationTimeCommandService.save(time);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationTimeResponse.from(reservationTime));
    }

    @GetMapping
    public ResponseEntity<ReservationTimeResponses> search() {
        List<ReservationTime> reservationTimes = reservationTimeQueryService.findAll();

        return ResponseEntity.ok()
                .body(ReservationTimeResponses.from(reservationTimes));
    }

    @GetMapping("/availability")
    public ResponseEntity<TimeWithStatusResponses> searchAvailableReservationTime(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        List<ReservationTimeAvailability> availabilities
                = reservationTimeQueryService.findWithAvailability(date, themeId);
        List<TimeWithStatusResponse> responses = availabilities.stream()
                .map(TimeWithStatusResponse::from)
                .toList();

        return ResponseEntity.ok()
                .body(TimeWithStatusResponses.of(responses));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationTimeCommandService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
