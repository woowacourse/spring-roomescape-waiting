package roomescape.time.presentation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.time.dto.ReservationTimeAddRequest;
import roomescape.time.dto.ReservationTimeResponse;
import roomescape.time.dto.ReservationTimeWithBookStatusResponse;
import roomescape.time.service.ReservationTimeService;

@RestController
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping("/times")
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimeList() {
        return ResponseEntity.ok(reservationTimeService.findAllReservationTime());
    }

    @PostMapping("/times")
    public ResponseEntity<ReservationTimeResponse> saveReservationTime(
            @Valid @RequestBody ReservationTimeAddRequest reservationTimeAddRequest) {
        ReservationTimeResponse saveResponse = reservationTimeService.saveReservationTime(reservationTimeAddRequest);
        URI createdUri = URI.create("/times/" + saveResponse.id());
        return ResponseEntity.created(createdUri).body(saveResponse);
    }

    @GetMapping("/times/available")
    public ResponseEntity<List<ReservationTimeWithBookStatusResponse>> readTimesStatus(
            @RequestParam(name = "date") LocalDate date,
            @RequestParam(name = "themeId") Long themeId) {
        return ResponseEntity.ok(reservationTimeService.findAllWithBookStatus(date, themeId));
    }

    @DeleteMapping("/times/{id}")
    public ResponseEntity<Void> removeReservationTime(@PathVariable("id") Long id) {
        reservationTimeService.removeReservationTime(id);
        return ResponseEntity.noContent().build();
    }
}
