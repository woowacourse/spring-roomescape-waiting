package roomescape.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.util.List;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.ReservationTimeRequest;
import roomescape.controller.dto.ReservationTimeResponse;
import roomescape.service.ReservationTimeService;
import roomescape.service.dto.ReservationTimeResult;

@RequestMapping("/admin/times")
@RestController
@Validated
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
    public ReservationTimeResponse create(@RequestBody @Valid ReservationTimeRequest request) {
        ReservationTimeResult saved = reservationTimeService.create(request.toCommand());
        return ReservationTimeResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable
                       @Positive(message = "id는 0보다 커야합니다.")
                       Long id) {
        reservationTimeService.delete(id);
    }
}
