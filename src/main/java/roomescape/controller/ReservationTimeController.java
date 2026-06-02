package roomescape.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
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
import roomescape.controller.dto.request.ControllerReservationTimeCreateRequest;
import roomescape.controller.dto.response.ReservationTimeAvailabilityResponse;
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
            @Valid @RequestBody ControllerReservationTimeCreateRequest requestDto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationTimeFacade.save(requestDto.toServiceReservationTimeRequest()));
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> findAll() {
        return ResponseEntity.ok(reservationTimeFacade.findAll());
    }

    @GetMapping("/available")
    public ResponseEntity<List<ReservationTimeAvailabilityResponse>> findAvailabilityByDateAndTheme(
            @RequestParam("date") LocalDate date, @RequestParam("themeId") Long themeId) {
        return ResponseEntity.ok(reservationTimeFacade.findAvailabilityByDateAndTheme(date, themeId));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        reservationTimeFacade.delete(id);
        return ResponseEntity
                .noContent()
                .build();
    }
}
