package roomescape.controller.admin;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.time.ReservationTimeRequest;
import roomescape.service.dto.time.ReservationTimeResponse;

@RestController
public class AdminReservationTimeRestController {

    private final ReservationTimeService reservationTimeService;

    public AdminReservationTimeRestController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/times")
    public ReservationTimeResponse createReservationTime(@Valid @RequestBody ReservationTimeRequest request) {
        return reservationTimeService.createReservationTime(request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/times/{id}")
    public void deleteReservationTime(@PathVariable long id) {
        reservationTimeService.deleteReservationTime(id);
    }
}
