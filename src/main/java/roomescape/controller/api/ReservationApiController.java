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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.service.dto.request.ReservationAdminSaveRequest;
import roomescape.service.dto.request.ReservationSaveRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.reservation.AdminReservationCreateService;
import roomescape.service.reservation.ReservationCreateService;
import roomescape.service.reservation.ReservationDeleteService;
import roomescape.service.reservation.ReservationFindService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@Validated
@RestController
public class ReservationApiController {

    private final ReservationCreateService reservationCreateService;
    private final AdminReservationCreateService adminReservationCreateService;
    private final ReservationFindService reservationFindService;
    private final ReservationDeleteService reservationDeleteService;

    public ReservationApiController(ReservationCreateService reservationCreateService,
                                    AdminReservationCreateService adminReservationCreateService,
                                    ReservationFindService reservationFindService,
                                    ReservationDeleteService reservationDeleteService) {
        this.reservationCreateService = reservationCreateService;
        this.adminReservationCreateService = adminReservationCreateService;
        this.reservationFindService = reservationFindService;
        this.reservationDeleteService = reservationDeleteService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservations() {
        List<Reservation> reservations = reservationFindService.findReservations();
        return ResponseEntity.ok(
                reservations.stream()
                        .map(ReservationResponse::new)
                        .toList()
        );
    }

    @GetMapping("/admin/reservations/search")
    public ResponseEntity<List<ReservationResponse>> getSearchingReservations(@RequestParam long memberId,
                                                                              @RequestParam long themeId,
                                                                              @RequestParam LocalDate dateFrom,
                                                                              @RequestParam LocalDate dateTo
    ) {
        List<Reservation> reservations = reservationFindService.searchReservations(memberId, themeId, dateFrom, dateTo);
        return ResponseEntity.ok(
                reservations.stream()
                        .map(ReservationResponse::new)
                        .toList()
        );
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
        Reservation newReservation = adminReservationCreateService.createReservation(request);
        return ResponseEntity.created(URI.create("/admin/reservations/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservation(@PathVariable
                                                  @Positive(message = "1 이상의 값만 입력해주세요.") long id) {
        reservationDeleteService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
