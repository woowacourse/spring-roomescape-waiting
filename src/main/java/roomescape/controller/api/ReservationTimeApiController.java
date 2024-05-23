package roomescape.controller.api;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.BookingStatus;
import roomescape.domain.ReservationTime;
import roomescape.service.dto.response.BookingStatusResponse;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.reservationtime.ReservationTimeFindService;

import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
public class ReservationTimeApiController {

    private final ReservationTimeFindService reservationTimeFindService;

    public ReservationTimeApiController(ReservationTimeFindService reservationTimeFindService) {
        this.reservationTimeFindService = reservationTimeFindService;
    }

    @GetMapping("/api/times")
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeFindService.findReservationTimes();
        return ResponseEntity.ok(
                reservationTimes.stream()
                        .map(ReservationTimeResponse::new)
                        .toList()
        );
    }

    @GetMapping("/api/times/available")
    public ResponseEntity<List<BookingStatusResponse>> getReservationTimesIsBooked(@RequestParam LocalDate date,
                                                                                   @RequestParam
                                                                                   @Positive(message = "1 이상의 값만 입력해주세요.")
                                                                                   long themeId) {
        BookingStatus bookingStatus = reservationTimeFindService.findIsBooked(date, themeId);
        return ResponseEntity.ok(
                bookingStatus.getReservationStatus()
                        .entrySet()
                        .stream()
                        .map(status -> new BookingStatusResponse(
                                        status.getKey(),
                                        status.getValue()
                                )
                        ).toList()
        );
    }
}
