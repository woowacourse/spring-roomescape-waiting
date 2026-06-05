package roomescape.reservationtime.adapter.in.web;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.reservationtime.application.ReservationTimeService;
import roomescape.reservationtime.application.dto.response.AvailableTimeFindResponse;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/user/times/availability")
@RequiredArgsConstructor
public class UserReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<AvailableTimeFindResponse>>> findTimesByDateAndThemeId(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam long themeId
    ) {
        List<AvailableTimeFindResponse> responses = reservationTimeService.findTimesByDateAndThemeId(date, themeId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }
}
