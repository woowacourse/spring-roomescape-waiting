package roomescape.domain.time.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.reservation.service.ReservationService;
import roomescape.domain.time.domain.ReservationTime;
import roomescape.domain.time.dto.BookableTimeResponse;
import roomescape.domain.time.dto.BookableTimesRequest;
import roomescape.domain.time.dto.ReservationTimeAddRequest;
import roomescape.domain.time.service.AdminReservationTimeService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
public class AdminReservationTimeController {

    private final ReservationService reservationService;
    private final AdminReservationTimeService adminReservationTimeService;

    public AdminReservationTimeController(ReservationService reservationService, AdminReservationTimeService adminReservationTimeService) {
        this.reservationService = reservationService;
        this.adminReservationTimeService = adminReservationTimeService;
    }

    @GetMapping("/times")
    public ResponseEntity<List<ReservationTime>> getReservationTimeList() {
        return ResponseEntity.ok(adminReservationTimeService.findAllReservationTime());
    }

    @PostMapping("/times")
    public ResponseEntity<ReservationTime> addReservationTime(
            @RequestBody ReservationTimeAddRequest reservationTimeAddRequest) {
        ReservationTime reservationTime = adminReservationTimeService.addReservationTime(reservationTimeAddRequest);
        return ResponseEntity.created(URI.create("/times/" + reservationTime.getId())).body(reservationTime);
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable("id") Long id) {
        adminReservationTimeService.removeReservationTime(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/times/bookable")
    public ResponseEntity<List<BookableTimeResponse>> getTimesWithStatus(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") Long themeId) {
        return ResponseEntity.ok(reservationService.findBookableTimes(new BookableTimesRequest(date, themeId)));
    }
}
