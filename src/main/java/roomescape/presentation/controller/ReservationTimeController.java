package roomescape.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
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
import roomescape.application.service.ReservationTimeService;
import roomescape.dto.request.ReservationTimeRegisterDto;
import roomescape.dto.response.AvailableReservationTimeResponseDto;
import roomescape.dto.response.ReservationTimeResponseDto;

@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationTimeResponseDto> getTimes() {
        return reservationTimeService.getAllTimes();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationTimeResponseDto addTime(
            @RequestBody @Valid ReservationTimeRegisterDto reservationTimeRegisterDto) {
        return reservationTimeService.saveTime(reservationTimeRegisterDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTime(@PathVariable("id") Long id) {
        reservationTimeService.deleteTime(id);
    }

    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    public List<AvailableReservationTimeResponseDto> findAvailableTimes(@RequestParam String date,
                                                                        @RequestParam Long themeId) {
        return reservationTimeService.getAvailableTimes(date, themeId);
    }
}
