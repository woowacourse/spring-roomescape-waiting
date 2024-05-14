package roomescape.web.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationTimeService;
import roomescape.service.request.ReservationTimeAppRequest;
import roomescape.service.response.BookableReservationTimeAppResponse;
import roomescape.service.response.ReservationTimeAppResponse;
import roomescape.web.controller.request.ReservationTimeWebRequest;
import roomescape.web.controller.response.BookableReservationTimeWebResponse;
import roomescape.web.controller.response.ReservationTimeWebResponse;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeWebResponse> create(@Valid @RequestBody ReservationTimeWebRequest request) {
        ReservationTimeAppResponse appResponse = reservationTimeService.save(
                new ReservationTimeAppRequest(request.startAt()));
        Long id = appResponse.id();

        return ResponseEntity.created(URI.create("/times/" + id))
                .body(new ReservationTimeWebResponse(id, appResponse.startAt()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBy(@PathVariable Long id) {
        reservationTimeService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeWebResponse>> getReservationTimes() {
        List<ReservationTimeAppResponse> appResponses = reservationTimeService.findAll();

        List<ReservationTimeWebResponse> reservationTimeWebResponses = appResponses.stream()
                .map(appResponse -> new ReservationTimeWebResponse(appResponse.id(),
                        appResponse.startAt()))
                .toList();

        return ResponseEntity.ok(reservationTimeWebResponses);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<BookableReservationTimeWebResponse>> getReservationTimesWithAvailability(
            @RequestParam String date, @RequestParam Long id) {

        List<BookableReservationTimeAppResponse> appResponses = reservationTimeService
                .findAllWithBookAvailability(date, id);

        List<BookableReservationTimeWebResponse> webResponses = appResponses.stream()
                .map(response -> new BookableReservationTimeWebResponse(
                        response.id(),
                        response.startAt(),
                        response.alreadyBooked()))
                .toList();

        return ResponseEntity.ok(webResponses);
    }
}
