package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.AvailableReservationTimeResponse;
import roomescape.dto.ReservationTimeRequest;
import roomescape.dto.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/times")
@RestController
public class TimeController {

    ReservationTimeService reservationTimeService;

    public TimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getTimes() {
        List<ReservationTimeResponse> responses = reservationTimeService.getAllReservationTimes();

        return ResponseEntity.ok()
                .body(responses);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> addTime(@RequestBody ReservationTimeRequest request) {
        ReservationTimeResponse response = reservationTimeService.addReservationTime(
                request);
        URI location = URI.create("/times/" + response.id());

        return ResponseEntity.created(location)
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable("id") Long id) {
        reservationTimeService.deleteReservationTimeById(id);

        return ResponseEntity.noContent()
                .build();
    }

    @GetMapping("/available") // todo: rest 한 api 이름으로 바꾸기
    public ResponseEntity<List<AvailableReservationTimeResponse>> getReservationTimeBookedStatus(
            @RequestParam LocalDate date,
            @RequestParam Long themeId) {
        List<AvailableReservationTimeResponse> responses = reservationTimeService.getReservationTimeBookedStatus(
                date, themeId);

        return ResponseEntity.ok()
                .body(responses);
    }
}
