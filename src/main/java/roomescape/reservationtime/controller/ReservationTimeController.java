package roomescape.reservationtime.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.reservationtime.service.ReservationTimeService;
import roomescape.reservationtime.service.dto.request.ReservationTimeCreateRequest;
import roomescape.reservationtime.service.dto.response.ReservationTimeResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/times")
@RequiredArgsConstructor
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getTimes() {
        final List<ReservationTimeResponse> results = reservationTimeService.getTimes();
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(
            @Valid @RequestBody ReservationTimeCreateRequest request
    ) {
        final ReservationTimeResponse result = reservationTimeService.create(request);
        return ResponseEntity.created(URI.create("/times"))
                .body(result);
    }

    @DeleteMapping("/{time-id}")
    public ResponseEntity<Void> delete(
            @PathVariable("time-id") Long timeId
    ) {
        reservationTimeService.delete(timeId);
        return ResponseEntity.noContent().build();
    }
}
