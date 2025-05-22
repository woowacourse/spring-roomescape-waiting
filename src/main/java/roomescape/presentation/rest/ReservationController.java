package roomescape.presentation.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.domain.user.User;
import roomescape.presentation.auth.Authenticated;
import roomescape.presentation.request.CreateReservationRequest;
import roomescape.presentation.response.ReservationResponse;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(final ReservationService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(CREATED)
    public ReservationResponse createReservation(
            @Authenticated final User user, @RequestBody @Valid final CreateReservationRequest request
    ) {
        Reservation reservation = service.saveReservation(user, request.date(), request.timeId(), request.themeId());
        return ReservationResponse.from(reservation);
    }

    @GetMapping
    public List<ReservationResponse> readAllReservations(
            @RequestParam(name = "themeId", required = false) final Long themeId,
            @RequestParam(name = "userId", required = false) final Long userId,
            @RequestParam(name = "dateFrom", required = false) final LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) final LocalDate dateTo
    ) {
        ReservationSearchFilter searchFilter = new ReservationSearchFilter(themeId, userId, dateFrom, dateTo);
        List<Reservation> reservations = service.findReservationsByFilter(searchFilter);

        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void deleteReservationById(@PathVariable("id") final long id) {
        service.removeById(id);
    }
}
