package roomescape.api;

import java.time.LocalDate;
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
import roomescape.dto.TimeWithStatusResponses;
import roomescape.facade.ReservationFacade;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;
    private final ReservationFacade reservationFacade;

    public ReservationTimeController(ReservationTimeService reservationTimeService,
                                     ReservationFacade reservationFacade) {
        this.reservationTimeService = reservationTimeService;
        this.reservationFacade = reservationFacade;
    }

    @GetMapping
    public ResponseEntity<ReservationTimeResponses> search() {
        return ResponseEntity.ok().body(ReservationTimeResponses.from(reservationTimeService.getReservationTimes()));
    }

    @GetMapping("/availability")
    public ResponseEntity<TimeWithStatusResponses> searchAvailableReservationTime(@RequestParam LocalDate date,
                                                                                  @RequestParam Long themeId) {
        return ResponseEntity.ok().body(
                TimeWithStatusResponses.of(reservationFacade.getTimesWithAvailability(date, themeId)));
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> add(@RequestBody ReservationTimeRequest request) {
        ReservationTime time = new ReservationTime(null, request.startAt());
        ReservationTimeResponse response = ReservationTimeResponse.from(reservationTimeService.addTime(time));

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationFacade.deleteTime(id);

        return ResponseEntity.noContent().build();
    }
}
