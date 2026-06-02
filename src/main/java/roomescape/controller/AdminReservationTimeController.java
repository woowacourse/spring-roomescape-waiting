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
import roomescape.dto.reservationtime.command.CreateReservationTimeCommand;
import roomescape.dto.reservationtime.request.CreateReservationTimeRequest;
import roomescape.dto.reservationtime.response.ReservationTimeResponse;
import roomescape.infrastructure.AdminOnly;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/admin/times")
@AdminOnly
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public AdminReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> createTime(@Valid @RequestBody CreateReservationTimeRequest createReservationTimeRequest) {
        ReservationTime createdTime = reservationTimeService.createReservationTime(
                CreateReservationTimeCommand.from(createReservationTimeRequest));
        URI location = URI.create("/times/" + createdTime.getId());
        return ResponseEntity.created(location).body(ReservationTimeResponse.from(createdTime));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTime(@PathVariable Long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.ok().build();
    }
}
