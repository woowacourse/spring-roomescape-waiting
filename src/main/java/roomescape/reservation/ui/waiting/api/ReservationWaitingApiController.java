package roomescape.reservation.ui.waiting.api;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.application.dto.LoginMemberInfo;
import roomescape.reservation.application.waiting.dto.ReservationWaitingCreateCommand;
import roomescape.reservation.application.waiting.dto.ReservationWaitingInfo;
import roomescape.reservation.application.waiting.service.ReservationWaitingService;
import roomescape.reservation.ui.waiting.dto.ReservationWaitingCreateRequest;
import roomescape.reservation.ui.waiting.dto.ReservationWaitingResponse;

@RestController
@RequestMapping("/reservations/waitings")
public class ReservationWaitingApiController {

    private final ReservationWaitingService reservationWaitingService;

    public ReservationWaitingApiController(final ReservationWaitingService reservationWaitingService) {
        this.reservationWaitingService = reservationWaitingService;
    }

    @PostMapping
    public ResponseEntity<ReservationWaitingResponse> create(
            @RequestBody @Valid final ReservationWaitingCreateRequest request,
            final LoginMemberInfo loginMemberInfo
    ) {
        final ReservationWaitingCreateCommand reservationWaitingCreateCommand = request.convertToCreateCommand(loginMemberInfo.id());
        final ReservationWaitingInfo reservationWaitingInfo = reservationWaitingService.createReservationWaiting(reservationWaitingCreateCommand);
        final URI uri = URI.create("/reservations/waitings/" + reservationWaitingInfo.id());
        final ReservationWaitingResponse reservationWaitingResponse = new ReservationWaitingResponse(reservationWaitingInfo);
        return ResponseEntity.created(uri).body(reservationWaitingResponse);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        reservationWaitingService.cancelReservationWaitingById(id);
        return ResponseEntity.noContent().build();
    }
}
