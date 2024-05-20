package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.request.ReservationTimeRequest;
import roomescape.controller.response.ReservationTimeResponse;
import roomescape.model.ReservationTime;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.ReservationTimeDto;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTime> times = reservationTimeService.findAllReservationTimes();
        List<ReservationTimeResponse> response = times.stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> addReservationTime(
            @Valid @RequestBody ReservationTimeRequest request) {
        ReservationTimeDto timeDto = ReservationTimeDto.from(request);
        ReservationTime time = reservationTimeService.saveReservationTime(timeDto);
        ReservationTimeResponse response = ReservationTimeResponse.from(time);
        return ResponseEntity
                .created(URI.create("/times/" + response.getId()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@NotNull @Min(1) @PathVariable("id") Long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent().build();
    }
}
