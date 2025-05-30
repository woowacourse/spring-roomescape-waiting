package roomescape.controller.api;

import java.time.LocalDate;
import java.util.List;
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
import roomescape.controller.annotation.AdminOnly;
import roomescape.dto.auth.LoginInfo;
import roomescape.dto.time.AvailableReservationTimeResponseDto;
import roomescape.dto.time.ReservationTimeCreateRequestDto;
import roomescape.dto.time.ReservationTimeResponseDto;
import roomescape.service.command.ReservationTimeCommandService;
import roomescape.service.query.ReservationTimeQueryService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeQueryService reservationTimeQueryService;
    private final ReservationTimeCommandService reservationTimeCommandService;

    public ReservationTimeController(ReservationTimeQueryService reservationTimeQueryService,
                                     ReservationTimeCommandService reservationTimeCommandService) {
        this.reservationTimeQueryService = reservationTimeQueryService;
        this.reservationTimeCommandService = reservationTimeCommandService;
    }

    @AdminOnly
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationTimeResponseDto> getAllReservationTimes(
    ) {
        return reservationTimeQueryService.findAllReservationTimes();
    }

    @GetMapping("/available")
    @ResponseStatus(HttpStatus.OK)
    public List<AvailableReservationTimeResponseDto> getAllReservationTimesWithAvailability(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") Long themeId
    ) {
        return reservationTimeQueryService.findAllReservationTimesWithAvailabilityBy(date, themeId);
    }

    @AdminOnly
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationTimeResponseDto addReservationTime(
            @RequestBody ReservationTimeCreateRequestDto requestDto
    ) {
        return reservationTimeCommandService.createReservationTime(requestDto);
    }

    @AdminOnly
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationTime(
            @PathVariable("id") Long id
    ) {
        reservationTimeCommandService.deleteReservationTimeById(id);
    }
}
