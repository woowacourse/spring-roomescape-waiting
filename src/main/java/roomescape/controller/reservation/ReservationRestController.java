package roomescape.controller.reservation;

import static roomescape.domain.reservation.ReservationStatus.CONFIRMED;

import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.AdminReservationRequest;
import roomescape.controller.dto.MemberReservationRequest;
import roomescape.controller.helper.AuthenticationPrincipal;
import roomescape.controller.helper.LoginMember;
import roomescape.repository.dto.ReservationRankResponse;
import roomescape.repository.dto.ReservationWaitingResponse;
import roomescape.service.ReservationService;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;
import roomescape.service.dto.reservation.ReservationSearchParams;

@RestController
public class ReservationRestController {

    private final ReservationService reservationService;

    public ReservationRestController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public List<ReservationRankResponse> findMemberReservations(@AuthenticationPrincipal LoginMember loginMember) {
        return reservationService.findReservationsByMemberEmail(loginMember.getEmail());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/reservations")
    public ReservationResponse createReservationMember(@AuthenticationPrincipal LoginMember loginMember,
                                                       @Valid @RequestBody MemberReservationRequest request) {
        ReservationCreate create = new ReservationCreate(
                loginMember.getEmail(),
                request.getThemeId(),
                request.getDate(),
                request.getTimeId()
        );
        return reservationService.createReservation(create);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/reservations/waiting")
    public ReservationResponse createReservationWaiting(@AuthenticationPrincipal LoginMember loginMember,
                                                        @Valid @RequestBody MemberReservationRequest request) {
        ReservationCreate create = new ReservationCreate(
                loginMember.getEmail(),
                request.getThemeId(),
                request.getDate(),
                request.getTimeId()
        );
        return reservationService.createReservation(create);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/reservations/waiting/{id}")
    public void deleteWaitingReservation(@AuthenticationPrincipal LoginMember loginMember,
                                         @PathVariable long id) {
        reservationService.deleteWaitingReservation(loginMember.getEmail(), id);
    }

    @GetMapping("/admin/reservations/confirmed")
    public List<ReservationResponse> searchConfirmedReservations(
            @RequestParam(name = "member", required = false) String email,
            @RequestParam(name = "theme", required = false) Long themeId,
            @RequestParam(name = "start-date", required = false) LocalDate dateFrom,
            @RequestParam(name = "end-date", required = false) LocalDate dateTo) {
        ReservationSearchParams request = new ReservationSearchParams(email, themeId, dateFrom, dateTo, CONFIRMED);
        return reservationService.searchConfirmedReservations(request);
    }

    @GetMapping("/admin/reservations/waiting")
    public List<ReservationWaitingResponse> findAllWaitingReservations() {
        return reservationService.findAllWaitingReservations();
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/admin/reservations")
    public ReservationResponse createReservationAdmin(@Valid @RequestBody AdminReservationRequest reservation) {
        return reservationService.createReservation(reservation.toCreateReservation());
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/admin/reservations/{id}")
    public void deleteReservation(@PathVariable long id) {
        reservationService.deleteConfirmedReservation(id);
    }
}
