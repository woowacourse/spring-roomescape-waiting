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
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        List<ReservationTimeResponse> responses = reservationTimeService.getReservationTimes()
                .stream()
                .map(time -> ReservationTimeResponse.from(time, false))
                .toList();

        return ResponseEntity.ok().body(responses);
    }

    @GetMapping(params = {"date", "themeId"})
    public ResponseEntity<List<ReservationTimeResponse>> searchTimeSlotsByDateAndTheme(@RequestParam LocalDate date,
                                                                                       @RequestParam Long themeId) {
        List<ReservationTimeResponse> reservationTimeResponses = reservationTimeService.getTimeSlotsWithReservationStatus(
                        date, themeId)
                .stream()
                .map(ReservationTimeResponse::of)
                .toList();
        return ResponseEntity.ok().body(reservationTimeResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> add(@Valid @RequestBody ReservationTimeRequest request) {
        ReservationTimeResponse response = ReservationTimeResponse.from(
                reservationTimeService.addReservationTime(request), false);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationTimeService.deleteReservationTime(id);

        return ResponseEntity.noContent().build();
    }
}
