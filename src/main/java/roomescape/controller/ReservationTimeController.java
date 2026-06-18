package roomescape.controller;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.AvailableTimeFindRequest;
import roomescape.controller.dto.request.ReservationTimeCreateRequest;
import roomescape.controller.dto.response.ReservationTimeResponse;
import roomescape.domain.reservation.ReservationTime;
import roomescape.service.ReservationTimeService;

@RestController
@RequiredArgsConstructor
public class ReservationTimeController {
    private final ReservationTimeService reservationTimeService;

    @PostMapping("/admin/times")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationTimeResponse create(@Valid @RequestBody ReservationTimeCreateRequest request) {
        ReservationTime found = reservationTimeService.create(request);

        return ReservationTimeResponse.from(found);
    }

    @GetMapping("/times")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationTimeResponse> findAll() {
        List<ReservationTime> reservationTimes = reservationTimeService.findAll();

        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @GetMapping("/times/available")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationTimeResponse> findAvailable(@Valid @ModelAttribute AvailableTimeFindRequest request) {
        List<ReservationTime> reservationTimes = reservationTimeService.findAvailable(request, LocalDate.now());

        return reservationTimes.stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @DeleteMapping("/admin/times/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable long id) {
        reservationTimeService.delete(id);
    }
}
