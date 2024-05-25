package roomescape.reservation.controller;

import jakarta.validation.Valid;
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
import roomescape.reservation.dto.request.CreateReservationTimeRequest;
import roomescape.reservation.dto.response.CreateReservationTimeResponse;
import roomescape.reservation.dto.response.FindReservationTimeResponse;
import roomescape.reservation.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<CreateReservationTimeResponse> createReservationTime(
            @Valid @RequestBody CreateReservationTimeRequest createReservationTimeRequest) {
        CreateReservationTimeResponse reservationTime = reservationTimeService.createReservationTime(
                createReservationTimeRequest);
        return ResponseEntity.created(URI.create("/times/" + reservationTime.id())).body(reservationTime);
    }

    @GetMapping
    public ResponseEntity<List<FindReservationTimeResponse>> getReservationTimes() {
        List<FindReservationTimeResponse> reservationTimeResponses = reservationTimeService.getReservationTimes();
        return ResponseEntity.ok(reservationTimeResponses);
    }

    @GetMapping("/{id}")
    public ResponseEntity<FindReservationTimeResponse> getReservationTime(@PathVariable Long id) {
        return ResponseEntity.ok(reservationTimeService.getReservationTime(id));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long id) {
        reservationTimeService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
