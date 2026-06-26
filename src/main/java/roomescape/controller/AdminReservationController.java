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
import roomescape.controller.dto.ReservationRequest;
import roomescape.controller.dto.ReservationResponse;
import roomescape.service.AdminReservationService;
import roomescape.service.dto.ReservationResult;

@RequestMapping("/admin/reservations")
@RestController
@Validated
public class AdminReservationController {

    private final AdminReservationService reservationService;

    public AdminReservationController(AdminReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping()
    public List<ReservationResponse> list() {
        return reservationService.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @PostMapping()
    public ReservationResponse create(@RequestBody @Valid ReservationRequest request) {
        ReservationResult saved = reservationService.create(request.toCommand());
        return ReservationResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    public void cancel(@PathVariable
                       @Positive(message = "id는 0보다 커야합니다.")
                       Long id) {
        reservationService.cancel(id);
    }
}
