package roomescape.controller;

import java.net.URI;
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
import roomescape.dto.business.ReservationTimeCreationContent;
import roomescape.dto.business.ReservationTimeWithBookState;
import roomescape.dto.request.ReservationTimeCreationRequest;
import roomescape.dto.response.ReservationTimeResponse;
import roomescape.dto.response.ReservationTimeWithBookStateResponse;
import roomescape.service.ReservationTimeService;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService timeService;

    public ReservationTimeController(ReservationTimeService timeService) {
        this.timeService = timeService;
    }

    @GetMapping
    public List<ReservationTimeResponse> findAllReservationTimes() {
        return timeService.findAllReservationTimes();
    }

    @GetMapping(params = {"themeId", "date"})
    public List<ReservationTimeWithBookStateResponse> findReservationTimesWithBookState(
            @RequestParam("themeId") Long themeId,
            @RequestParam("date") LocalDate date
    ) {
        List<ReservationTimeWithBookState> reservations =
                timeService.findReservationTimesWithBookState(themeId, date);
        return reservations.stream()
                .map(ReservationTimeWithBookStateResponse::new)
                .toList();
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> addReservationTime(
            @RequestBody ReservationTimeCreationRequest request
    ) {
        ReservationTimeCreationContent creationContent = new ReservationTimeCreationContent(request);
        ReservationTimeResponse reservationTimeResponse = timeService.addReservationTime(creationContent);
        return ResponseEntity.status(HttpStatus.CREATED)
                .location(URI.create("/times/" + reservationTimeResponse.id()))
                .body(reservationTimeResponse);
    }

    @DeleteMapping("/{reservationTimeId}")
    public ResponseEntity<Void> deleteReservationTimeById(
            @PathVariable("reservationTimeId") Long id
    ) {
        timeService.deleteReservationTimeById(id);
        return ResponseEntity.noContent().build();
    }
}
