package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.reservation.ReservationTimeRequest;
import roomescape.service.dto.reservation.ReservationTimeResponse;
import roomescape.service.dto.time.AvailableTimeRequest;
import roomescape.service.dto.time.AvailableTimeResponses;

@RestController
public class ReservationTimeRestController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeRestController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    public List<ReservationTimeResponse> findReservationTimes() {
        return reservationTimeService.findAllReservationTimes();
    }

    @GetMapping("/times/available")
    public AvailableTimeResponses findAvailableReservationTimes(@Valid AvailableTimeRequest request) {
        return reservationTimeService.findAvailableReservationTimes(request);
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
