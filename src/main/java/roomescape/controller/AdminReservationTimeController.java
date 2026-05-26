package roomescape.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.ReservationTime;
import roomescape.dto.reservationtime.CreateReservationTimeRequest;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/admin/times")
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public AdminReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<Void> createTime(@Valid @RequestBody CreateReservationTimeRequest createReservationTimeRequest) {
        ReservationTime createdTime = reservationTimeService.createReservationTime(createReservationTimeRequest);
        URI location = URI.create("/times/" + createdTime.getId());
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable Long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.ok().build();
    }
}
