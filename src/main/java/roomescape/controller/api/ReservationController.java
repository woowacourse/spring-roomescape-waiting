package roomescape.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.annotation.Login;
import roomescape.domain.Member;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.service.ReservationService;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    private ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(@Login Member member, @RequestBody ReservationRequest reservationRequest) {
        return ReservationResponse.from(reservationService.createReservationAfterNow(reservationRequest, member));
    }

//    @PostMapping("/admin")
//    @ResponseStatus(HttpStatus.CREATED)
//    private ReservationResponse createAdminReservation(@RequestBody ReservationRequest reservationRequest) {
//        return ReservationResponse.from(reservationService.createReservation(reservationRequest));
//    }

    @GetMapping
    public List<ReservationResponse> readReservations() {
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
