package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.controller.dto.ReservationTimeCreateRequest;
import roomescape.reservation.controller.dto.ReservationTimeResponse;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.reservation.service.dto.AvailableTimeInfo;
import roomescape.reservation.service.dto.ReservationTimeCreateCommand;
import roomescape.reservation.service.dto.ReservationTimeInfo;

@RestController
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping("/times")
    public ResponseEntity<ReservationTimeResponse> create(@RequestBody @Valid final ReservationTimeCreateRequest request) {
        ReservationTimeCreateCommand command = request.toCreateCommand();
        ReservationTimeInfo timeInfo = reservationTimeService.createReservationTime(command);
        URI uri = URI.create("/times/" + timeInfo.id());
        ReservationTimeResponse response = new ReservationTimeResponse(timeInfo);
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        List<ReservationTimeInfo> timeInfos = reservationTimeService.getReservationTimes();
        List<ReservationTimeResponse> responses = timeInfos.stream()
                .map(ReservationTimeResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        reservationTimeService.deleteReservationTimeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/available-times")
    public ResponseEntity<List<AvailableTimeInfo>> findAvailableTimes(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") long themeId
    ) {
        List<AvailableTimeInfo> responses = reservationTimeService.findAvailableTimes(date, themeId);
        return ResponseEntity.ok().body(responses);
    }
}
