package roomescape.presentation.rest;

import static org.springframework.http.HttpStatus.CREATED;
import static org.springframework.http.HttpStatus.NO_CONTENT;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.AllArgsConstructor;
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
import roomescape.domain.auth.AuthenticationInfo;
import roomescape.domain.reservation.ReservationSearchFilter;
import roomescape.exception.AuthorizationException;
import roomescape.presentation.request.CreateReservationRequest;
import roomescape.presentation.response.ReservationResponse;

@RestController
@RequestMapping("/reservations")
@AllArgsConstructor
public class ReservationController {

    private final ReservationService service;

    @PostMapping
    @ResponseStatus(CREATED)
    public ReservationResponse reserve(
            final AuthenticationInfo authenticationInfo,
            @RequestBody @Valid final CreateReservationRequest request
    ) {
        var reservation = service.reserve(authenticationInfo.id(), request.date(), request.timeId(), request.themeId());
        return ReservationResponse.from(reservation);
    }

    @PostMapping("/wait")
    @ResponseStatus(CREATED)
    public ReservationResponse waitFor(
            final AuthenticationInfo authenticationInfo,
            @RequestBody @Valid final CreateReservationRequest request
    ) {
        var reservation = service.waitFor(authenticationInfo.id(), request.date(), request.timeId(), request.themeId());
        return ReservationResponse.from(reservation);
    }

    @GetMapping
    public List<ReservationResponse> getAllReservations(
            @RequestParam(name = "themeId", required = false) final Long themeId,
            @RequestParam(name = "userId", required = false) final Long userId,
            @RequestParam(name = "dateFrom", required = false) final LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) final LocalDate dateTo
    ) {
        var searchFilter = new ReservationSearchFilter(themeId, userId, dateFrom, dateTo);
        var reservations = service.findAllReservations(searchFilter);
        return ReservationResponse.from(reservations);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(NO_CONTENT)
    public void delete(
        final AuthenticationInfo authenticationInfo,
        @PathVariable("id") final long id
    ) {
        if (!authenticationInfo.isAdmin()) {
            throw new AuthorizationException("관리자에게만 허용된 작업입니다.");
        }
        service.removeByIdForce(id);
    }
}
