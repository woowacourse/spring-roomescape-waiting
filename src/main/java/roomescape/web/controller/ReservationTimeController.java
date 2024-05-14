package roomescape.web.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import roomescape.service.ReservationTimeService;
import roomescape.web.dto.request.time.ReservationTimeRequest;
import roomescape.web.dto.response.time.AvailableReservationTimeResponse;
import roomescape.web.dto.response.time.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
@RequiredArgsConstructor
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAllReservationTime() {
        List<ReservationTimeResponse> response = reservationTimeService.findAllReservationTime();
        return ResponseEntity.ok().body(response);
    }

    @GetMapping("/available")
    public ResponseEntity<List<AvailableReservationTimeResponse>> findAllAvailableReservationTime(
            @RequestParam(name = "date") LocalDate date, @RequestParam(name = "theme-id") Long themeId) {
        List<AvailableReservationTimeResponse> response =
                reservationTimeService.findAllAvailableReservationTime(date, themeId);
        return ResponseEntity.ok().body(response);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> saveReservationTime(
            @Valid @RequestBody ReservationTimeRequest request) {
        ReservationTimeResponse response = reservationTimeService.saveReservationTime(request);
        return ResponseEntity.created(URI.create("/times/" + response.id())).body(response);
    }

    @DeleteMapping("/{time_id}")
    public ResponseEntity<Void> deleteReservationTime(@PathVariable(value = "time_id") Long id) {
        reservationTimeService.deleteReservationTime(id);
        return ResponseEntity.noContent().build();
    }
}
