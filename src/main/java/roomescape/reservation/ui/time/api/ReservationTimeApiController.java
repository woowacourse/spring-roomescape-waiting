package roomescape.reservation.ui.time.api;

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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.time.dto.ReservationTimeAvailabilityInfo;
import roomescape.reservation.application.time.dto.ReservationTimeCreateCommand;
import roomescape.reservation.application.time.dto.ReservationTimeInfo;
import roomescape.reservation.application.time.service.ReservationTimeService;
import roomescape.reservation.ui.time.dto.ReservationTimeCreateRequest;
import roomescape.reservation.ui.time.dto.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeApiController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeApiController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(
            @RequestBody @Valid final ReservationTimeCreateRequest request) {
        final ReservationTimeCreateCommand command = request.toCreateCommand();
        final ReservationTimeInfo timeInfo = reservationTimeService.createReservationTime(command);
        final URI uri = URI.create("/times/" + timeInfo.id());
        final ReservationTimeResponse response = new ReservationTimeResponse(timeInfo);
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        final List<ReservationTimeInfo> timeInfos = reservationTimeService.getReservationTimes();
        final List<ReservationTimeResponse> responses = timeInfos.stream()
                .map(ReservationTimeResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        reservationTimeService.deleteReservationTimeById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/availability")
    public ResponseEntity<List<ReservationTimeAvailabilityInfo>> findAvailableTimes(
            @RequestParam("date") final LocalDate date,
            @RequestParam("themeId") final long themeId
    ) {
        final List<ReservationTimeAvailabilityInfo> responses = reservationTimeService.findAvailableTimes(date, themeId);
        return ResponseEntity.ok().body(responses);
    }
}
