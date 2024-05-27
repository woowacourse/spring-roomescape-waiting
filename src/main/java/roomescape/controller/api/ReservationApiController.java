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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationWaitingWithRank;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.MemberReservationResponse;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.reservation.ReservationService;

import java.net.URI;
import java.util.List;

@Validated
@RequestMapping("/api")
@RestController
public class ReservationApiController {

    private final ReservationService reservationService;

    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<Reservation> reservations = reservationService.findReservations();
        return ResponseEntity.ok(
                reservations.stream()
                        .map(ReservationResponse::new)
                        .toList()
        );
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MemberReservationResponse>> getMemberReservations(@AuthenticatedMember Member member) {
        List<ReservationWaitingWithRank> reservationWaitingWithRanks =
                reservationService.findMemberReservations(member.getId());
        return ResponseEntity.ok(
                reservationWaitingWithRanks.stream()
                        .map(MemberReservationResponse::new)
                        .toList()
        );
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addReservationByMember(@RequestBody @Valid
                                                                      ReservationSaveRequest request,
                                                                      @AuthenticatedMember Member member) {
        Reservation newReservation = reservationService.createReservation(
                request,
                member
        );
        return ResponseEntity.created(URI.create("/api/reservations/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteWaiting(@AuthenticatedMember Member member,
                                              @PathVariable
                                              @Positive(message = "1 이상의 값만 입력해주세요.")
                                              long id) {
        reservationService.deleteReservation(id, member);
        return ResponseEntity.noContent().build();
    }
}
