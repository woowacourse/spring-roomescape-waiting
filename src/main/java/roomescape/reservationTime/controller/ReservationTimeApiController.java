package roomescape.reservationTime.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservationTime.dto.admin.ReservationTimeRequest;
import roomescape.reservationTime.dto.admin.ReservationTimeResponse;
import roomescape.reservationTime.dto.user.AvailableReservationTimeRequest;
import roomescape.reservationTime.dto.user.AvailableReservationTimeResponse;
import roomescape.reservationTime.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeApiController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeApiController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        return ResponseEntity.ok(reservationTimeService.findAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findByDateAndTheme(
            @ModelAttribute AvailableReservationTimeRequest availableReservationTimeRequest
    ) {
        return ResponseEntity.ok(reservationTimeService.findByDateAndTheme(availableReservationTimeRequest));
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> add(@RequestBody ReservationTimeRequest reservationTimeRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(reservationTimeService.add(reservationTimeRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteById(@PathVariable("id") Long id) {
        reservationTimeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
