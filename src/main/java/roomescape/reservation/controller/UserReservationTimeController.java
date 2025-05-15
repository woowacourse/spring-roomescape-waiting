package roomescape.reservation.controller;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.application.UserReservationTimeService;
import roomescape.reservation.application.dto.response.ReservationTimeServiceResponse;
import roomescape.reservation.controller.dto.response.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class UserReservationTimeController {

    private final UserReservationTimeService userReservationTimeService;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<ReservationTimeResponse> getAllWithStatus(
            @RequestParam("themeId") Long themeId,
            @RequestParam("date") LocalDate date
    ) {
        List<ReservationTimeServiceResponse> responses = userReservationTimeService.getAllWithStatus(themeId, date);
        return responses.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }
}
