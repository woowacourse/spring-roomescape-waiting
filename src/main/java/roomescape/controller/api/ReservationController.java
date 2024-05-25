package roomescape.controller.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.response.ApiResponses;
import roomescape.controller.support.Auth;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.response.PersonalReservationResponse;
import roomescape.dto.response.ReservationResponse;
import roomescape.security.Accessor;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ApiResponses<ReservationResponse> getReservationsByConditions(@RequestParam(required = false) Long memberId,
                                                                         @RequestParam(required = false) Long themeId,
                                                                         @RequestParam(required = false) LocalDate dateFrom,
                                                                         @RequestParam(required = false) LocalDate dateTo) {
        List<ReservationResponse> reservationResponses = reservationService
                .getReservationsByConditions(memberId, themeId, dateFrom, dateTo);
        return new ApiResponses<>(reservationResponses);
    }

    @GetMapping("/mine")
    public ApiResponses<PersonalReservationResponse> getMyReservations(@Auth Accessor accessor) {
        List<PersonalReservationResponse> reservationResponses = reservationService
                .getReservationsByMemberId(accessor.id());
        return new ApiResponses<>(reservationResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody @Valid ReservationRequest request,
                                                              @Auth Accessor accessor) {
        long memberId = accessor.id();
        ReservationResponse response = reservationService.addReservation(request.toCreateReservationRequest(memberId));
        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationById(@PathVariable Long id) {
        reservationService.deleteReservationById(id);
    }
}
