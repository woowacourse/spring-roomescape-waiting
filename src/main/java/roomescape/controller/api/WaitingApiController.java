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
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationDeleteService;

import java.net.URI;

@RestController
public class WaitingApiController {

    private final ReservationCreateService reservationCreateService;
    private final ReservationDeleteService reservationDeleteService;


    public WaitingApiController(ReservationCreateService reservationCreateService,
                                ReservationDeleteService reservationDeleteService) {
        this.reservationCreateService = reservationCreateService;
        this.reservationDeleteService = reservationDeleteService;
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> addWaiting(@RequestBody @Valid ReservationSaveRequest request,
                                                          @AuthenticatedMember Member member) {
        Reservation newReservation = reservationCreateService.createReservation(request, member, ReservationStatus.WAITING);
        return ResponseEntity.created(URI.create("/waiting/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }

    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteWaiting(@PathVariable
                                              @Positive(message = "1 이상의 값만 입력해주세요.") long id) {
        reservationDeleteService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
