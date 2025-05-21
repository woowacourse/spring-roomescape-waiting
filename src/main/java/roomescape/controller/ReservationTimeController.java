package roomescape.controller;

import jakarta.validation.Valid;
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
import roomescape.service.dto.ReservationTimeRequest;
import roomescape.service.dto.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationService) {
        this.reservationTimeService = reservationService;
    }

    @GetMapping()
    public ResponseEntity<List<ReservationTimeResponse>> reservationTimeList() {
        return ResponseEntity.status(HttpStatus.OK).body(reservationTimeService.findReservationTimes());
    }

    @PostMapping()
    public ResponseEntity<ReservationTimeResponse> reservationTimeAdd(
            @RequestBody @Valid ReservationTimeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationTimeService.addReservationTime(request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> reservationTimeRemove(@PathVariable(name = "id") long id) {
        reservationTimeService.removeReservationTime(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}
