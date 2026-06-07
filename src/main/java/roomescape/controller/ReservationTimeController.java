package roomescape.controller;

import java.time.LocalDate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.controller.dto.response.ReservationTimeAvailabilityListResponse;
import roomescape.controller.dto.response.ReservationTimeListResponse;
import roomescape.controller.dto.response.ReservationTimeResponse;
import roomescape.facade.ReservationTimeFacade;

@RestController
@RequestMapping(value = "/times")
public class ReservationTimeController {

    private final ReservationTimeFacade reservationTimeFacade;

    public ReservationTimeController(ReservationTimeFacade reservationTimeFacade) {
        this.reservationTimeFacade = reservationTimeFacade;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> save(
            @RequestBody ReservationTimeCreateRequest request) {
        ReservationTimeResponse response = reservationTimeFacade.save(request);
        return ResponseEntity.
                status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping
    public ResponseEntity<ReservationTimeListResponse> findAll() {
        ReservationTimeListResponse response = reservationTimeFacade.findAll();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/available")
    public ResponseEntity<ReservationTimeAvailabilityListResponse> findAvailabilityByDateAndTheme(
            @RequestParam("date") LocalDate date, @RequestParam("themeId") Long themeId) {

        ReservationTimeAvailabilityListResponse response = reservationTimeFacade.findAvailabilityByDateAndTheme(date,
                themeId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        reservationTimeFacade.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
