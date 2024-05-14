package roomescape.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.service.dto.request.ReservationTimeSaveRequest;
import roomescape.service.dto.response.ReservationStatusResponse;
import roomescape.service.dto.response.ReservationTimeResponse;
import roomescape.service.reservationtime.ReservationTimeCreateService;
import roomescape.service.reservationtime.ReservationTimeDeleteService;
import roomescape.service.reservationtime.ReservationTimeFindService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
public class ReservationTimeApiController {

    private final ReservationTimeCreateService reservationTimeCreateService;
    private final ReservationTimeFindService reservationTimeFindService;
    private final ReservationTimeDeleteService reservationTimeDeleteService;

    public ReservationTimeApiController(ReservationTimeCreateService reservationTimeCreateService,
                                        ReservationTimeFindService reservationTimeFindService,
                                        ReservationTimeDeleteService reservationTimeDeleteService) {
        this.reservationTimeCreateService = reservationTimeCreateService;
        this.reservationTimeFindService = reservationTimeFindService;
        this.reservationTimeDeleteService = reservationTimeDeleteService;
    }

    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTime> reservationTimes = reservationTimeFindService.findReservationTimes();
        return ResponseEntity.ok(
                reservationTimes.stream()
                        .map(ReservationTimeResponse::new)
                        .toList()
        );
    }

    @GetMapping("/times/available")
    public ResponseEntity<List<ReservationStatusResponse>> getReservationTimesIsBooked(
            @RequestParam LocalDate date,
            @RequestParam @Positive(message = "1 이상의 값만 입력해주세요.") long themeId) {
        ReservationStatus reservationStatus = reservationTimeFindService.findIsBooked(date, themeId);
        return ResponseEntity.ok(
                reservationStatus.getReservationStatus()
                        .keySet()
                        .stream()
                        .map(reservationTime -> new ReservationStatusResponse(
                                reservationTime,
                                reservationStatus.findReservationStatusBy(reservationTime))
                        ).toList());
    }

    @PostMapping("/times")
    public ResponseEntity<ReservationTimeResponse> addReservationTime(@RequestBody @Valid ReservationTimeSaveRequest request) {
        ReservationTime reservationTime = reservationTimeCreateService.createReservationTime(request);
        return ResponseEntity.created(URI.create("times/" + reservationTime.getId()))
                .body(new ReservationTimeResponse(reservationTime));
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable
                                                      @Positive(message = "1 이상의 값만 입력해주세요.") long id) {
        reservationTimeDeleteService.deleteReservationTime(id);
        return ResponseEntity.noContent().build();
    }
}
