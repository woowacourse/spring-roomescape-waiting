package roomescape.time.controller;

import java.net.URI;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.time.controller.dto.ReservationTimeRequest;
import roomescape.time.controller.dto.ReservationTimeResponse;
import roomescape.time.service.ReservationTimeService;
import roomescape.time.service.dto.ReservationTimeResult;

@RestController
@RequestMapping("/admin/times")
public class ReservationTimeAdminController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeAdminController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> create(@RequestBody ReservationTimeRequest requestDto) {
        ReservationTimeResult reservationTime = reservationTimeService.save(requestDto.toCommand());
        ReservationTimeResponse response = ReservationTimeResponse.from(reservationTime);
        return ResponseEntity
                .created(URI.create("/times/" + reservationTime.id()))
                .body(response);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        reservationTimeService.deleteById(id);
    }
}
