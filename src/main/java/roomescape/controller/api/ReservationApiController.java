package roomescape.controller.api;

import jakarta.validation.Valid;
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
import roomescape.controller.api.validator.IdPositive;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationWaitWithRank;
import roomescape.domain.member.Member;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.reservation.UserReservationResponses;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationDeleteService;
import roomescape.service.reservation.ReservationFindService;
import roomescape.service.reservationwait.ReservationWaitFindService;

@Validated
@RestController
public class ReservationApiController {

    private final ReservationFindService reservationFindService;
    private final ReservationCreateService reservationCreateService;
    private final ReservationDeleteService reservationDeleteService;
    private final ReservationWaitFindService reservationWaitFindService;

    public ReservationApiController(ReservationFindService reservationFindService,
                                    ReservationCreateService reservationCreateService,
                                    ReservationDeleteService reservationDeleteService,
                                    ReservationWaitFindService reservationWaitFindService) {
        this.reservationFindService = reservationFindService;
        this.reservationCreateService = reservationCreateService;
        this.reservationDeleteService = reservationDeleteService;
        this.reservationWaitFindService = reservationWaitFindService;
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<UserReservationResponses> getUserReservations(@AuthenticatedMember Member member) {
        List<Reservation> userReservations = reservationFindService.findUserReservations(member.getId());
        List<ReservationWaitWithRank> userReservationWaits = reservationWaitFindService.findUserReservationWaits(
                member.getId());
        return ResponseEntity.ok(UserReservationResponses.of(userReservations, userReservationWaits));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody @Valid ReservationSaveRequest request,
                                                              @AuthenticatedMember Member member) {
        Reservation newReservation = reservationCreateService.create(request, member);
        return ResponseEntity.created(URI.create("/reservations/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable
                                                  @IdPositive long reservationId) {
        reservationDeleteService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}
