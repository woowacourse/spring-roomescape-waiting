package roomescape.reservation.ui.reservation.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.application.dto.LoginMemberInfo;
import roomescape.reservation.application.reservation.dto.ReservationCreateCommand;
import roomescape.reservation.application.reservation.dto.ReservationInfo;
import roomescape.reservation.application.reservation.dto.ReservationSearchCondition;
import roomescape.reservation.application.reservation.service.ReservationService;
import roomescape.reservation.ui.reservation.dto.AdminReservationCreateRequest;
import roomescape.reservation.ui.reservation.dto.MemberReservationResponse;
import roomescape.reservation.ui.reservation.dto.ReservationCreateRequest;
import roomescape.reservation.ui.reservation.dto.ReservationResponse;
import roomescape.reservation.ui.reservation.dto.ReservationSearchConditionRequest;

@RestController
@RequestMapping("/reservations")
public class ReservationApiController {

    private final ReservationService reservationService;

    public ReservationApiController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(
            @RequestBody @Valid final ReservationCreateRequest request,
            final LoginMemberInfo loginMemberInfo
    ) {
        final ReservationCreateCommand reservationCreateCommand = request.convertToCreateCommand(loginMemberInfo.id());
        final ReservationInfo reservationInfo = reservationService.createReservation(reservationCreateCommand);
        final URI uri = URI.create("/reservations/" + reservationInfo.id());
        final ReservationResponse response = new ReservationResponse(reservationInfo);
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> findAll(
            @ModelAttribute final ReservationSearchConditionRequest request) {
        final ReservationSearchCondition condition = request.toCondition();
        final List<ReservationInfo> reservationInfos = reservationService.getReservations(condition);
        final List<ReservationResponse> responses = reservationInfos.stream()
                .map(ReservationResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MemberReservationResponse>> findAllMine(final LoginMemberInfo loginMemberInfo) {
        final List<ReservationInfo> reservationInfos = reservationService.findReservationsByMemberId(
                loginMemberInfo.id());
        final List<MemberReservationResponse> responses = reservationInfos.stream()
                .map(MemberReservationResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        reservationService.cancelReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
