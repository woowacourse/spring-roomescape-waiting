package roomescape.reservation.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.model.Principal;
import roomescape.global.annotation.Login;
import roomescape.member.dto.response.MemberGetResponse;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.MyReservationGetResponse;
import roomescape.reservation.dto.response.ReservationGetResponse;
import roomescape.reservation.model.Theme;
import roomescape.reservation.service.ReservationService;

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
    public ReservationGetResponse createReservation(@RequestBody @Valid ReservationCreateRequest requestBody, @Login Principal principal) {
        return ReservationGetResponse.from(reservationService.createReservationAfterNow(requestBody, principal.memberId()));
    }

    @GetMapping
    public List<ReservationGetResponse> readAllReservations() {
        return reservationService.findAllReservations().stream()
                .map(ReservationGetResponse::from)
                .toList();
    }

    @GetMapping("/mine")
    public List<MyReservationGetResponse> readMyReservations(@Login Principal principal) {
        return reservationService.findByMemberId(principal.memberId()).stream()
                .map(reservation -> new MyReservationGetResponse(reservation.getId(),
                        MemberGetResponse.from(reservation.getMember()),
                        reservation.getDate(),
                        reservation.getTime(),
                        reservation.getTheme(),
                        "Reserved"))
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable("id") Long id) {
        reservationService.deleteReservationById(id);
    }

    @GetMapping("/popular-themes")
    public List<Theme> readMostReservedThemes() {
        return reservationService.findMostReservedThemes();
    }
}
