package roomescape.web.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.service.ReservationTimeService;
import roomescape.service.request.ReservationTimeSaveDto;
import roomescape.service.response.BookableReservationTimeDto;
import roomescape.service.response.ReservationTimeDto;
import roomescape.web.controller.request.ReservationTimeRequest;
import roomescape.web.controller.response.BookableReservationTimeResponse;
import roomescape.web.controller.response.ReservationTimeResponse;

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
    public ResponseEntity<ReservationTimeResponse> create(@Valid @RequestBody ReservationTimeRequest request) {
        ReservationTimeDto appResponse = reservationTimeService.save(
                new ReservationTimeSaveDto(request.startAt()));
        Long id = appResponse.id();

        return ResponseEntity.created(URI.create("/times/" + id))
                .body(new ReservationTimeResponse(id, appResponse.startAt()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBy(@PathVariable Long id) {
        reservationTimeService.delete(id);

        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getReservationTimes() {
        List<ReservationTimeDto> appResponses = reservationTimeService.findAll();

        List<ReservationTimeResponse> reservationTimeRespons = appResponses.stream()
                .map(appResponse -> new ReservationTimeResponse(appResponse.id(),
                        appResponse.startAt()))
                .toList();

        return ResponseEntity.ok(reservationTimeRespons);
    }

    @GetMapping("/availability")
    public ResponseEntity<List<BookableReservationTimeResponse>> getReservationTimesWithAvailability(
            @RequestParam String date, @RequestParam Long id) {

        List<BookableReservationTimeDto> appResponses = reservationTimeService
                .findAllWithBookAvailability(date, id);

        List<BookableReservationTimeResponse> webResponses = appResponses.stream()
                .map(response -> new BookableReservationTimeResponse(
                        response.id(),
                        response.startAt(),
                        response.alreadyBooked()))
                .toList();

        return ResponseEntity.ok(webResponses);
    }
}
