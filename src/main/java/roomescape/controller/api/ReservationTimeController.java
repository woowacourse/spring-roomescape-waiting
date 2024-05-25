package roomescape.controller.api;

import jakarta.validation.Valid;
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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ReservationTimeRequest;
import roomescape.controller.dto.response.ApiResponses;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.response.AvailableReservationTimeResponse;
import roomescape.service.dto.response.ReservationTimeResponse;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public ReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping
    public ApiResponses<ReservationTimeResponse> getAllReservationTimes() {
        List<ReservationTimeResponse> reservationTimeResponses = reservationTimeService.getAllReservationTimes();
        return new ApiResponses<>(reservationTimeResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationTimeResponse> addReservationTime(
            @RequestBody @Valid ReservationTimeRequest request) {
        ReservationTimeResponse response = reservationTimeService.addReservationTime(
                request.toCreateReservationTimeRequest());
        return ResponseEntity.created(URI.create("/times/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationTimeById(@PathVariable Long id) {
        reservationTimeService.deleteReservationTimeById(id);
    }

    @GetMapping("/available")
    public ApiResponses<AvailableReservationTimeResponse> getAvailableReservationTimes(@RequestParam LocalDate date,
                                                                                       @RequestParam Long themeId) {
        List<AvailableReservationTimeResponse> availableReservationTimeResponses = reservationTimeService
                .getAvailableReservationTimes(date, themeId);
        return new ApiResponses<>(availableReservationTimeResponses);
    }
}
