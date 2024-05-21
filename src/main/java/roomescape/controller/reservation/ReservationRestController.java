package roomescape.controller.reservation;

import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.controller.dto.MemberReservationRequest;
import roomescape.controller.helper.AuthenticationPrincipal;
import roomescape.controller.helper.LoginMember;
import roomescape.repository.dto.ReservationRankResponse;
import roomescape.service.ReservationService;
import roomescape.service.dto.reservation.ReservationCreate;
import roomescape.service.dto.reservation.ReservationResponse;

@RestController
public class ReservationRestController {

    private final ReservationService reservationService;

    public ReservationRestController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public List<ReservationRankResponse> findReservations(@AuthenticationPrincipal LoginMember loginMember) {
        return reservationService.findReservationsByMemberEmail(loginMember.getEmail());
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping("/reservations")
    public ReservationResponse createReservation(@AuthenticationPrincipal LoginMember loginMember,
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
    public ReservationResponse createWaitingReservation(@AuthenticationPrincipal LoginMember loginMember,
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
}
