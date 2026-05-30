package roomescape.reservation.controller;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.dto.response.ReservationTimeCreateResponse;
import roomescape.reservation.dto.response.ReservationTimeFindAllResponse;
import roomescape.reservation.service.ReservationFacade;

@RestController
@RequestMapping("/times")
public class ReservationTimeController {

    private final ReservationFacade reservationFacade;

    public ReservationTimeController(ReservationFacade reservationFacade) {
        this.reservationFacade = reservationFacade;
    }

    @PostMapping
    public ResponseEntity<ReservationTimeCreateResponse> create(@RequestBody ReservationTimeCreateRequest reservationTime) {
        ReservationTimeCreateResponse saved = reservationFacade.createReservationTime(reservationTime);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping
    public ResponseEntity<List<ReservationTimeFindAllResponse>> findAll() {
        return ResponseEntity.ok(reservationFacade.findAllReservationTime());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        reservationFacade.deleteReservationTime(id);
        return ResponseEntity.ok().build();
    }
}

