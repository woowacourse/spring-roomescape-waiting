package roomescape.controller.api;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.service.dto.request.ReservationAdminSaveRequest;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.reservation.ReservationResponse;
import roomescape.service.dto.response.reservation.ReservationResponses;
import roomescape.service.dto.response.reservation.UserReservationResponses;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationDeleteService;
import roomescape.service.reservation.ReservationFindService;

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

    @GetMapping("/reservations")
    public ResponseEntity<ReservationResponses> getReservations() {
        List<Reservation> reservations = reservationFindService.findReservations();
        return ResponseEntity.ok(ReservationResponses.from(reservations));
    }

    @GetMapping("/admin/reservations/search")
    public ResponseEntity<ReservationResponses> getSearchingReservations(@RequestParam long memberId,
                                                                              @RequestParam long themeId,
                                                                              @RequestParam LocalDate dateFrom,
                                                                              @RequestParam LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationFindService.searchReservations(memberId, themeId, dateFrom, dateTo);
        return ResponseEntity.ok(ReservationResponses.from(reservations));
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<UserReservationResponses> getUserReservations(@AuthenticatedMember Member member) {
        List<Reservation> userReservations = reservationFindService.findUserReservations(member.getId());
        return ResponseEntity.ok(UserReservationResponses.from(userReservations));
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addReservationByUser(@RequestBody @Valid ReservationSaveRequest request,
                                                                    @AuthenticatedMember Member member) {
        Reservation newReservation = reservationCreateService.createReservation(request, member);
        return ResponseEntity.created(URI.create("/reservations/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> addReservationByAdmin(@RequestBody @Valid ReservationAdminSaveRequest request) {
        Reservation newReservation = reservationCreateService.createReservation(request);
        return ResponseEntity.created(URI.create("/admin/reservations/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }

    @DeleteMapping("/reservations/{reservationId}")
    public ResponseEntity<Void> deleteReservation(@PathVariable
                                                  @Positive(message = "1 이상의 값만 입력해주세요.") long reservationId) {
        reservationDeleteService.deleteReservation(reservationId);
        return ResponseEntity.noContent().build();
    }
}
