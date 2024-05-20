package roomescape.presentation.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.ReservationService;
import roomescape.application.dto.request.ReservationRequest;
import roomescape.application.dto.request.ReservationWaitingRequest;
import roomescape.application.dto.response.MyReservationResponse;
import roomescape.application.dto.response.ReservationResponse;
import roomescape.presentation.Auth;
import roomescape.presentation.dto.Accessor;
import roomescape.presentation.dto.request.ReservationWaitingWebRequest;
import roomescape.presentation.dto.request.ReservationWebRequest;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> getMyReservationWithRanks(@Auth Accessor accessor) {
        List<MyReservationResponse> myReservationResponses = reservationService
                .getMyReservationWithRanks(accessor.id());

        return ResponseEntity.ok(myReservationResponses);
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody @Valid ReservationWebRequest request,
            @Auth Accessor accessor
    ) {
        ReservationRequest reservationRequest = request.toReservationRequest(accessor.id());
        ReservationResponse reservationResponse = reservationService.addReservation(reservationRequest);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }
}
