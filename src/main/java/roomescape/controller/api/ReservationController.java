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
import roomescape.controller.dto.request.ReservationRequest;
import roomescape.controller.dto.response.ApiResponses;
import roomescape.controller.support.Auth;
import roomescape.security.authentication.Authentication;
import roomescape.service.ReservationService;
import roomescape.service.dto.response.PersonalReservationResponse;
import roomescape.service.dto.response.ReservationResponse;

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
    public ApiResponses<PersonalReservationResponse> getMyReservations(@Auth Authentication authentication) {
        List<PersonalReservationResponse> reservationResponses = reservationService
                .getReservationsByMemberId(authentication.getId());
        return new ApiResponses<>(reservationResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody @Valid ReservationRequest request,
                                                              @Auth Authentication authentication) {
        long memberId = authentication.getId();
        ReservationResponse response = reservationService.addReservation(
                request.toReservationCreationRequest(memberId));
        return ResponseEntity.created(URI.create("/reservations/" + response.id()))
                .body(response);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservationById(@PathVariable Long id) {
        reservationService.deleteReservationById(id);
    }
}
