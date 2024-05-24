package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;
import roomescape.controller.dto.AdminReservationRequest;
import roomescape.controller.dto.MemberReservationRequest;
import roomescape.controller.helper.AuthenticationPrincipal;
import roomescape.controller.helper.LoginMember;
import roomescape.service.ReservationService;
import roomescape.service.WaitingService;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;
import roomescape.service.dto.waiting.WaitingResponse;

@RestController
public class ReservationRestController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public ReservationRestController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @GetMapping("/admin/reservations")
    public List<ReservationResponse> findReservations(
            @RequestParam(name = "member", required = false) String email,
            @RequestParam(name = "theme", required = false) Long themeId,
            @RequestParam(name = "start-date", required = false) LocalDate dateFrom,
            @RequestParam(name = "end-date", required = false) LocalDate dateTo) {

        ReservationSearchParams request = new ReservationSearchParams(email, themeId, dateFrom, dateTo);
        return reservationService.findAllReservations(request);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/reservations")
    public ReservationResponse createReservationMember(@AuthenticationPrincipal LoginMember loginMember,
                                                       @Valid @RequestBody MemberReservationRequest request) {
        return reservationService.createReservation(new ReservationCreate(loginMember, request));
    }

    @GetMapping("/reservations")
    public List<ReservationResponse> findMemberReservations(@AuthenticationPrincipal LoginMember loginMember) {
        return reservationService.findReservationsByMemberEmail(loginMember.getEmail());
    }

    @PostMapping("/reservations/waitings")
    public ResponseEntity<WaitingResponse> createWaiting(@AuthenticationPrincipal LoginMember loginMember,
                                                         @Valid @RequestBody MemberReservationRequest request) {
        WaitingResponse response = waitingService.createWaiting(new ReservationCreate(loginMember, request));
        URI uri = UriComponentsBuilder.fromPath("/reservations/{reservationId}")
                .build(response.reservationId());

        return ResponseEntity.created(uri).body(response);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/reservations")
    public ReservationResponse createReservationAdmin(@Valid @RequestBody AdminReservationRequest reservation) {
        return reservationService.createReservation(reservation.toCreateReservation());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/reservations/{id}")
    public void deleteReservation(@PathVariable long id) {
        reservationService.deleteReservation(id);
    }
}
