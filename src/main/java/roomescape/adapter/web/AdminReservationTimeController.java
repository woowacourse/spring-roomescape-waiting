package roomescape.adapter.web;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.adapter.web.dto.request.ReservationTimeRequest;
import roomescape.adapter.web.dto.response.ReservationTimeResponse;
import roomescape.application.ReservationTimeService;
import roomescape.application.dto.result.ReservationTimeResult;

@RequestMapping("/admin/times")
@RestController
public class AdminReservationTimeController {

    private final ReservationTimeService reservationTimeService;

    public AdminReservationTimeController(ReservationTimeService reservationTimeService) {
        this.reservationTimeService = reservationTimeService;
    }

    @GetMapping()
    public List<ReservationTimeResponse> list() {
        return reservationTimeService.findAll().stream()
                .map(ReservationTimeResponse::from)
                .toList();
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationTimeResponse create(@RequestBody @Valid ReservationTimeRequest request) {
        ReservationTimeResult saved = reservationTimeService.create(request.toCommand());
        return ReservationTimeResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reservationTimeService.delete(id);
    }
}
