package roomescape.controller.api;

import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.BookingStatus;
import roomescape.domain.ReservationTime;
import roomescape.service.dto.response.BookingStatusResponse;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.reservationtime.ReservationTimeService;

import java.time.LocalDate;
import java.util.List;

@Validated
@RequestMapping("/api/times")
@RestController
public class ReservationTimeApiController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeApiController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeService.findReservationTimes();
        return ResponseEntity.ok(
                reservationTimes.stream()
                        .map(ReservationTimeResponse::new)
                        .toList()
        );
    }

    @GetMapping("/available")
    public ResponseEntity<List<BookingStatusResponse>> getReservationTimesIsBooked(@RequestParam LocalDate date,
                                                                                   @RequestParam
                                                                                   @Positive(message = "1 이상의 값만 입력해주세요.")
                                                                                   long themeId) {
        BookingStatus bookingStatus = reservationTimeService.findTimeSlotsBookingStatus(date, themeId);
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
