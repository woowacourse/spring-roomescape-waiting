package roomescape.api;

import jakarta.validation.Valid;
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
import roomescape.application.ReservationApplicationService;
import roomescape.domain.ReservationTime;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.dto.ReservationTimeResponses;
import roomescape.dto.TimeWithStatusResponses;
import roomescape.service.ReservationTimeService;

import java.time.LocalDate;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;
    private final ReservationApplicationService reservationApplicationService;

    public ReservationTimeController(ReservationTimeService reservationTimeService,
                                     ReservationApplicationService reservationApplicationService) {
        this.reservationTimeService = reservationTimeService;
        this.reservationApplicationService = reservationApplicationService;
    }

    @GetMapping
    public ResponseEntity<ReservationTimeResponses> search() {
        return ResponseEntity.ok().body(ReservationTimeResponses.from(reservationTimeService.getReservationTimes()));
    }

    @GetMapping("/availability")
    public ResponseEntity<TimeWithStatusResponses> searchAvailableReservationTime(@RequestParam LocalDate date,
                                                                                  @RequestParam Long themeId) {
        return ResponseEntity.ok().body(
                TimeWithStatusResponses.of(reservationApplicationService.getTimesWithAvailability(date, themeId)));
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> add(@RequestBody @Valid ReservationTimeRequest request) {
        ReservationTime time = new ReservationTime(null, request.startAt());
        ReservationTimeResponse response = ReservationTimeResponse.from(reservationTimeService.addTime(time));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationApplicationService.deleteTime(id);

        return ResponseEntity.noContent().build();
    }
}
