package roomescape.controller.api;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.time.AvailableReservationTimeResponseDto;
import roomescape.dto.time.ReservationTimeCreateRequestDto;
import roomescape.dto.time.ReservationTimeResponseDto;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationTimeResponseDto> getAllReservationTimes() {
        return reservationTimeService.findAllReservationTimes();
    }

    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    public List<AvailableReservationTimeResponseDto> getAvailableReservationTimes(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") Long themeId
    ) {
        return reservationTimeService.findAvailableReservationTimes(date, themeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationTimeResponseDto addReservationTime(
            @RequestBody final ReservationTimeCreateRequestDto requestDto) {
        return reservationTimeService.createReservationTime(requestDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationTime(
            @PathVariable("id") Long id
    ) {
        reservationTimeService.deleteReservationTimeById(id);
    }
}
