package roomescape.controller.reservationtime;

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
import roomescape.controller.reservationtime.dto.ReservationTimeCreateRequest;
import roomescape.controller.reservationtime.dto.ReservationTimeResponse;
import roomescape.service.reservationtime.ReservationTimeService;

@RestController
@RequestMapping("/admin/reservation-times")
public class ReservationTimeAdminController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeAdminController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTimeResponse> reservationTimes = reservationTimeService.getAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
        return ResponseEntity.ok()
                .body(reservationTimes);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createReservationTime(
            @Valid @RequestBody final ReservationTimeCreateRequest request
    ) {
        ReservationTimeResponse reservationTime = ReservationTimeResponse.from(
                reservationTimeService.save(request.startAt())
        );

        return ResponseEntity.created(URI.create("/admin/reservation-times/" + reservationTime.id()))
                .body(reservationTime);
    }

    @DeleteMapping("/{timeId}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable final Long timeId) {
        reservationTimeService.deleteById(timeId);
        return ResponseEntity.noContent()
                .build();
    }
}
