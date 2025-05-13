package roomescape.reservation.controller;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.response.ReservationTimeGetResponse;
import roomescape.reservation.dto.response.ReservationTimeWithIsBookedGetResponse;
import roomescape.reservation.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    private ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationTimeGetResponse createReservationTime(@RequestBody @Valid ReservationTimeCreateRequest requestBody) {
        return ReservationTimeGetResponse.from(reservationTimeService.createReservationTime(requestBody));
    }

    @GetMapping
    public List<ReservationTimeGetResponse> readAllReservationTimes() {
        return reservationTimeService.findAllReservationTimes().stream()
            .map(ReservationTimeGetResponse::from)
            .toList();
    }

    @GetMapping("/themes/{themeId}")
    public List<ReservationTimeWithIsBookedGetResponse> readReservationTimesByDateAndThemeIdWithIsBooked(@PathVariable("themeId") Long themeId, @RequestParam("date") LocalDate date) {
        return reservationTimeService.findReservationTimeByDateAndThemeIdWithIsBooked(date, themeId);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationTimeById(@PathVariable("id") Long id) {
        reservationTimeService.deleteReservationTimeById(id);
    }
}
