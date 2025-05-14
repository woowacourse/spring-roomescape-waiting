package roomescape.presentation.rest;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import roomescape.application.ReservationService;
import roomescape.domain.User;
import roomescape.domain.repository.ReservationSearchFilter;
import roomescape.presentation.Authenticated;
import roomescape.presentation.request.CreateReservationRequest;
import roomescape.presentation.response.ReservationResponse;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService service;

    public ReservationController(final ReservationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> reserve(
            @Authenticated final User user,
            @RequestBody @Valid final CreateReservationRequest request
    ) {
        var reservation = service.reserve(user, request.date(), request.timeId(), request.themeId());
        var response = ReservationResponse.from(reservation);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.id())).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> getAllReservations(
            @RequestParam(name = "themeId", required = false) Long themeId,
            @RequestParam(name = "userId", required = false) Long userId,
            @RequestParam(name = "dateFrom", required = false) LocalDate dateFrom,
            @RequestParam(name = "dateTo", required = false) LocalDate dateTo
    ) {

        var searchFilter = new ReservationSearchFilter(themeId, userId, dateFrom, dateTo);
        var reservations = service.findAllReservations(searchFilter);
        var response = ReservationResponse.from(reservations);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        service.removeById(id);
        return ResponseEntity.noContent().build();
    }
}
