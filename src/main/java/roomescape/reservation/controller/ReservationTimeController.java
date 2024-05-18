package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.reservation.controller.dto.request.ReservationTimeSaveRequest;
import roomescape.reservation.controller.dto.response.ReservationTimeDeleteResponse;
import roomescape.reservation.controller.dto.response.ReservationTimeResponse;
import roomescape.reservation.controller.dto.response.SelectableTimeResponse;
import roomescape.reservation.service.ReservationTimeService;

@Controller
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(final ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> save(
            @RequestBody @Valid final ReservationTimeSaveRequest reservationTimeSaveRequest
    ) {
        ReservationTimeResponse reservationTimeResponse =
                ReservationTimeResponse.from(reservationTimeService.save(reservationTimeSaveRequest));
        return ResponseEntity.created(URI.create("/times/" + reservationTimeResponse.id()))
                .body(reservationTimeResponse);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeResponse>> getAll() {
        List<ReservationTimeResponse> reservationTimeResponses =
                ReservationTimeResponse.list(reservationTimeService.getAll());
        return ResponseEntity.ok(reservationTimeResponses);
    }

    @GetMapping("/selectable")
    public ResponseEntity<List<SelectableTimeResponse>> findSelectableTimes(
            @RequestParam(name = "date") final LocalDate date,
            @RequestParam(name = "themeId") final long themeId
    ) {
        List<SelectableTimeResponse> selectableTimeResponses =
                SelectableTimeResponse.list(reservationTimeService.findSelectableTimes(date, themeId));
        return ResponseEntity.ok(selectableTimeResponses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ReservationTimeDeleteResponse> delete(@PathVariable("id") final long id) {
        ReservationTimeDeleteResponse reservationTimeDeleteResponse =
                new ReservationTimeDeleteResponse(reservationTimeService.delete(id));
        return ResponseEntity.ok().body(reservationTimeDeleteResponse);
    }
}
