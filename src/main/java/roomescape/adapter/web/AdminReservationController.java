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
import roomescape.adapter.web.dto.request.ReservationRequest;
import roomescape.adapter.web.dto.response.ReservationResponse;
import roomescape.application.ReservationService;
import roomescape.application.dto.result.ReservationResult;

@RequestMapping("/admin/reservations")
@RestController
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping()
    public List<ReservationResponse> list() {
        return reservationService.findAll().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@RequestBody @Valid ReservationRequest request) {
        ReservationResult saved = reservationService.create(request.toCommand());
        return ReservationResponse.from(saved);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        reservationService.delete(id);
    }
}
