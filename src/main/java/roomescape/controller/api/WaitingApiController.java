package roomescape.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.reservation.ReservationService;

import java.net.URI;

@RestController
public class WaitingApiController {

    private final ReservationService reservationService;

    public WaitingApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/api/waitings")
    public ResponseEntity<ReservationResponse> addWaiting(@AuthenticatedMember Member member,
                                                          @RequestBody @Valid
                                                          ReservationSaveRequest request) {
        Reservation newReservation = reservationService.createReservation(
                request,
                member,
                ReservationStatus.WAITING
        );
        return ResponseEntity.created(URI.create("/api/waitings/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }

    @DeleteMapping("/api/waitings/{id}")
    public ResponseEntity<Void> deleteWaiting(@AuthenticatedMember Member member,
                                              @PathVariable
                                              @Positive(message = "1 이상의 값만 입력해주세요.")
                                              long id) {
        reservationService.deleteReservation(id, member);
        return ResponseEntity.noContent().build();
    }
}
