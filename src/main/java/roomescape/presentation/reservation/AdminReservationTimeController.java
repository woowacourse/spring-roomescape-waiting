package roomescape.presentation.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.reservation.ReservationTimeService;
import roomescape.presentation.reservation.request.TimeCreateRequest;
import roomescape.presentation.reservation.response.ReservationTimesResponse;
import roomescape.presentation.reservation.response.TimeCreateResponse;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/times")
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @GetMapping
    public ResponseEntity<ReservationTimesResponse> getAllReservationTimes() {
        return ResponseEntity.ok(reservationTimeService.getAllReservationTime());
    }

    @PostMapping
    public ResponseEntity<TimeCreateResponse> createReservationTime(
            @Valid @RequestBody TimeCreateRequest request
    ) {
        TimeCreateResponse response = reservationTimeService.createReservationTime(request);
        return ResponseEntity.created(URI.create("/times/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{timeId}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable Long timeId) {
        reservationTimeService.deleteReservationTime(timeId);
        return ResponseEntity.noContent().build();
    }
}
