package roomescape.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.command.ReservationTimeCreateCommand;
import roomescape.service.dto.result.ReservationTimeResult;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResult>> getTimes() {
        final List<ReservationTimeResult> results = reservationTimeService.getTimes();
        return ResponseEntity.ok(results);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResult> create(
            @Valid @RequestBody final ReservationTimeCreateCommand request
    ) {
        final ReservationTimeResult result = reservationTimeService.create(request);
        return ResponseEntity.created(URI.create("/times/" + result.id()))
                .body(result);
    }

    @DeleteMapping("/{time-id}")
    public ResponseEntity<Void> delete(
            @PathVariable("time-id") final Long timeId
    ) {
        reservationTimeService.delete(timeId);
        return ResponseEntity.noContent().build();
    }
}
