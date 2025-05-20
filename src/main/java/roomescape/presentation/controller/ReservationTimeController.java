package roomescape.presentation.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.business.service.ReservationTimeService;
import roomescape.presentation.dto.PlayTimeRequest;
import roomescape.presentation.dto.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(
            @RequestBody final PlayTimeRequest playTimeRequest
    ) {
        final ReservationTimeResponse reservationTimeResponse = reservationTimeService.insert(playTimeRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(reservationTimeResponse);

    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> readAll() {
        final List<ReservationTimeResponse> reservationTimeResponse = reservationTimeService.findAll();

        return ResponseEntity.ok(reservationTimeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Long id) {
        reservationTimeService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
