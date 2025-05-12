package roomescape.reservation.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.annotation.Login;
import roomescape.member.model.Member;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    private ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @Login
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@RequestBody ReservationCreateRequest reservationCreateRequest, Member member) {
        return ReservationResponse.from(reservationService.createReservationAfterNow(reservationCreateRequest, member));
    }

    @GetMapping
    public List<ReservationResponse> readAllReservations() {
        return reservationService.findAllReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable("id") Long id) {
        reservationService.deleteReservationById(id);
    }
}
