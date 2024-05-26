package roomescape.controller.api.admin;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthenticatedMember;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.service.dto.request.ReservationAdminSaveRequest;
import roomescape.service.dto.response.ReservationResponse;
import roomescape.service.dto.response.WaitingResponse;
import roomescape.service.reservation.AdminReservationService;
import roomescape.service.reservation.ReservationService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RequestMapping("/api/admin/reservations")
@RestController
public class AdminReservationApiController {

    private final AdminReservationService adminReservationService;
    private final ReservationService reservationService;

    public AdminReservationApiController(AdminReservationService adminReservationService,
                                         ReservationService reservationService) {
        this.adminReservationService = adminReservationService;
        this.reservationService = reservationService;
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponse>> getSearchingReservations(@RequestParam long memberId,
                                                                              @RequestParam long themeId,
                                                                              @RequestParam LocalDate dateFrom,
                                                                              @RequestParam LocalDate dateTo) {
        List<Reservation> reservations = reservationService.searchReservations(memberId, themeId, dateFrom, dateTo);
        return ResponseEntity.ok(
                reservations.stream()
                        .map(ReservationResponse::new)
                        .toList()
        );
    }

    @GetMapping("/waiting-list")
    public ResponseEntity<List<WaitingResponse>> getWaiting(@AuthenticatedMember Member member) {
        List<Reservation> waitings = reservationService.findWaitings();
        return ResponseEntity.ok(
                waitings.stream()
                        .map(WaitingResponse::new)
                        .toList()
        );
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservationByAdmin(@RequestBody @Valid
                                                                     ReservationAdminSaveRequest request) {
        Reservation newReservation = adminReservationService.createReservation(request);
        return ResponseEntity.created(URI.create("/api/admin/reservations/" + newReservation.getId()))
                .body(new ReservationResponse(newReservation));
    }
}
