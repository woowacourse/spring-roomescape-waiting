package roomescape.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.reservationtime.AvailableReservationTimeResponse;
import roomescape.dto.reservationtime.ReservationTimeRequest;
import roomescape.dto.reservationtime.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
public class ReservationTimeRestController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeRestController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeResponse>> readAll() {
        return ResponseEntity.ok(reservationTimeService.readAll());
    }

    @GetMapping("/times/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> readAvailable(
            @RequestParam LocalDate date,
            @RequestParam Long themeId
    ) {
        return ResponseEntity.ok(reservationTimeService.readAvailable(date, themeId));
    }

    @PostMapping("/admin/times")
    public ResponseEntity<ReservationTimeResponse> create(@RequestBody ReservationTimeRequest reservationTimeReq) {
        ReservationTimeResponse newReservationTime = reservationTimeService.create(reservationTimeReq);
        URI uri = URI.create("/times/" + newReservationTime.getId());
        return ResponseEntity.created(uri).body(newReservationTime);
    }

    @PutMapping("/admin/times/{id}")
    public ResponseEntity<Void> update(@RequestBody ReservationTimeRequest newReservationTimeReq, @PathVariable Long id) {
        reservationTimeService.update(newReservationTimeReq, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/admin/times/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationTimeService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
