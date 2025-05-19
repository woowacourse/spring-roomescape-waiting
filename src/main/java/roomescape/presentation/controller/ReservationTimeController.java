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
import roomescape.presentation.dto.PlayTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<PlayTimeResponse> create(
            @RequestBody final PlayTimeRequest playTimeRequest
    ) {
        final PlayTimeResponse playTimeResponse = reservationTimeService.insert(playTimeRequest);

        return ResponseEntity.status(HttpStatus.CREATED).body(playTimeResponse);

    }

    @GetMapping
    public ResponseEntity<List<PlayTimeResponse>> readAll() {
        final List<PlayTimeResponse> playTimeResponse = reservationTimeService.findAll();

        return ResponseEntity.ok(playTimeResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final Long id) {
        reservationTimeService.deleteById(id);

        return ResponseEntity.noContent().build();
    }
}
