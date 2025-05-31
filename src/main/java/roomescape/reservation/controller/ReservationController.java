package roomescape.reservation.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import roomescape.auth.dto.LoginMember;
import roomescape.reservation.dto.MyReservationResponse;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSearchRequest;
import roomescape.reservation.service.ReservationService;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
@Validated
public class ReservationController {

    private final ReservationService reservationService;

    @GetMapping
    public List<ReservationResponse> findReservationsByCriteria(
            @ModelAttribute final ReservationSearchRequest request) {
        return reservationService.findReservationsByCriteria(request);
    }

    @GetMapping("/times")
    public List<AvailableReservationTimeResponse> findAllAvailableTimes(
            @NotNull @RequestParam final LocalDate date,
            @NotNull @RequestParam final Long themeId
    ) {
        return reservationService.findAllReservationTime(date, themeId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse saveReservation(
            @Valid @RequestBody final ReservationRequest request,
            final LoginMember member
    ) {
        return reservationService.saveReservation(request, member);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable final Long id) {
        reservationService.deleteReservation(id);
    }

    @GetMapping("/mine")
    public List<MyReservationResponse> findMyReservations(final LoginMember loginMember) {
        return reservationService.findMyReservations(loginMember);
    }
}
