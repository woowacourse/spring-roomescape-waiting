package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationTimeService;
import roomescape.dto.ReservationTimeCreateCommand;
import roomescape.dto.ReservationTimeResult;

import java.util.List;

@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @PostMapping
    public ResponseEntity<ReservationTimeResult> create(
            @Valid @RequestBody ReservationTimeCreateCommand request
    ) {
        final ReservationTimeResult result = reservationTimeService.create(request);
        return ResponseEntity.created(URI.create("/times/" + result.id()))
                .body(result);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResult>> getTimes() {
        final List<ReservationTimeResult> results = reservationTimeService.getTimes();
        return ResponseEntity.ok(results);
    }

    @DeleteMapping("/{time-id}")
    public ResponseEntity<Void> delete(
            @PathVariable("time-id") final Long timeId
    ) {
        reservationTimeService.delete(timeId);
        return ResponseEntity.noContent().build();
    }
}
