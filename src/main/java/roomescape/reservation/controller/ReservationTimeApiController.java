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
import roomescape.common.dto.ResourcesResponse;
import roomescape.reservation.dto.request.TimeSaveRequest;
import roomescape.reservation.dto.response.AvailableReservationTimeResponse;
import roomescape.reservation.dto.response.TimeResponse;
import roomescape.reservation.service.ReservationTimeService;

@RestController
public class ReservationTimeApiController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeApiController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    public ResponseEntity<ResourcesResponse<TimeResponse>> findAll() {
        List<TimeResponse> times = reservationTimeService.findAll();
        ResourcesResponse<TimeResponse> response = new ResourcesResponse<>(times);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/times/available")
    public ResponseEntity<ResourcesResponse<AvailableReservationTimeResponse>> findAvailableTimes(
            @RequestParam("date") LocalDate date,
            @RequestParam("theme-id") Long themeId
    ) {
        List<AvailableReservationTimeResponse> availableTimes = reservationTimeService.findAvailableTimes(date, themeId);
        ResourcesResponse<AvailableReservationTimeResponse> response = new ResourcesResponse<>(availableTimes);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/times")
    public ResponseEntity<TimeResponse> save(@Valid @RequestBody TimeSaveRequest timeSaveRequest) {
        TimeResponse timeResponse = reservationTimeService.save(timeSaveRequest);

        return ResponseEntity.created(URI.create("/times/" + timeResponse.id())).body(timeResponse);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        reservationTimeService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
