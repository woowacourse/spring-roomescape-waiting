package roomescape.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
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
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWaitingWithRank;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.UserReservationResponse;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationDeleteService;
import roomescape.service.reservation.ReservationFindService;

import java.net.URI;
import java.util.List;

@Validated
@RestController
public class ReservationApiController {

    private final ReservationCreateService reservationCreateService;
    private final ReservationFindService reservationFindService;
    private final ReservationDeleteService reservationDeleteService;

    public ReservationApiController(ReservationCreateService reservationCreateService,
                                    ReservationFindService reservationFindService,
                                    ReservationDeleteService reservationDeleteService) {
        this.reservationCreateService = reservationCreateService;
        this.reservationFindService = reservationFindService;
        this.reservationDeleteService = reservationDeleteService;
    }

    @GetMapping("/api/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<Reservation> reservations = reservationFindService.findReservations();
        return ResponseEntity.ok(
                reservations.stream()
                        .map(ReservationResponse::new)
                        .toList()
        );
    }

    @GetMapping("/api/reservations-mine")
    public ResponseEntity<List<UserReservationResponse>> getUserReservations(@AuthenticatedMember Member member) {
        List<ReservationWaitingWithRank> reservationWaitingWithRanks =
                reservationFindService.findMemberReservations(member.getId());
        return ResponseEntity.ok(
                reservationWaitingWithRanks.stream()
                        .map(UserReservationResponse::new)
                        .toList()
        );
    }

    @PostMapping("/api/reservations")
    public ResponseEntity<ReservationResponse> addReservationByUser(@RequestBody @Valid
                                                                    ReservationSaveRequest request,
                                                                    @AuthenticatedMember Member member) {
        Reservation newReservation = reservationCreateService.createReservation(
                request,
                member,
                ReservationStatus.RESERVED
        );
        return ResponseEntity.created(URI.create("/api/reservations/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }

    @DeleteMapping("/api/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable
                                                  @Positive(message = "1 이상의 값만 입력해주세요.")
                                                  long id) {
        reservationDeleteService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
