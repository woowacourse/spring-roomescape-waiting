package roomescape.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.reservation.UserReservationResponses;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationDeleteService;
import roomescape.service.reservation.ReservationFindService;

@Validated
@RestController
public class ReservationApiController {

    private final ReservationFindService reservationFindService;
    private final ReservationCreateService reservationCreateService;
    private final ReservationDeleteService reservationDeleteService;

    public ReservationApiController(ReservationFindService reservationFindService,
                                    ReservationCreateService reservationCreateService,
                                    ReservationDeleteService reservationDeleteService) {
        this.reservationFindService = reservationFindService;
        this.reservationCreateService = reservationCreateService;
        this.reservationDeleteService = reservationDeleteService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<UserReservationResponses> getUserReservations(@AuthenticatedMember Member member) {
        List<Reservation> userReservations = reservationFindService.findUserReservations(member.getId());
        return ResponseEntity.ok(UserReservationResponses.from(userReservations));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody @Valid ReservationSaveRequest request,
                                                              @AuthenticatedMember Member member) {
        Reservation newReservation = reservationCreateService.createReservation(request, member);
        return ResponseEntity.created(URI.create("/reservations/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable
                                                  @Positive(message = "1 이상의 값만 입력해주세요.") long reservationId) {
        reservationDeleteService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}
