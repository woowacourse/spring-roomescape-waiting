package roomescape.controller;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.request.ReservationCreateRequest;
import roomescape.controller.dto.request.ReservationUpdateRequest;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.domain.reservation.RankedReservation;
import roomescape.service.ReservationService;

@RestController
public class ReservationController {
    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse create(@Valid @RequestBody ReservationCreateRequest request) {
        RankedReservation reservation = reservationService.reserve(request, LocalDateTime.now());

        return ReservationResponse.from(reservation);
    }

    @GetMapping("/reservations")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponse> findList(@RequestParam(required = false) String name) {
        List<RankedReservation> reservations = reservationService.findList(name);
        System.out.println("예약 건 수 : " + reservations.size());

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @GetMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponse find(@PathVariable long id) {
        RankedReservation reservation = reservationService.find(id);
        return ReservationResponse.from(reservation);
    }

    @DeleteMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable Long id) {
        reservationService.cancel(id, LocalDateTime.now());
    }

    @PutMapping("/reservations/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ReservationResponse update(@Valid @RequestBody ReservationUpdateRequest request, @PathVariable long id) {
        RankedReservation updated = reservationService.update(request, id, LocalDateTime.now());
        return ReservationResponse.from(updated);
    }

    @GetMapping("/test")
    public void test() {
        reservationService.find(1L);
    }
}
