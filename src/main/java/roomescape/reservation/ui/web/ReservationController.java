package roomescape.reservation.ui.web;

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
import roomescape.reservation.ui.dto.AdminReservationCreateRequest;
import roomescape.reservation.ui.dto.MemberReservationResponse;
import roomescape.reservation.ui.dto.ReservationCreateRequest;
import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.reservation.ui.dto.ReservationSearchConditionRequest;
import roomescape.reservation.application.service.ReservationService;
import roomescape.reservation.application.dto.ReservationCreateCommand;
import roomescape.reservation.application.dto.ReservationInfo;
import roomescape.reservation.application.dto.ReservationSearchCondition;

@RestController
@RequestMapping
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping("/reservations")
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

    @PostMapping("/admin/reservations")
    public ResponseEntity<ReservationResponse> create(@RequestBody @Valid final AdminReservationCreateRequest request) {
        final ReservationInfo reservationInfo = reservationService.createReservation(request.convertToCreateCommand());
        final URI uri = URI.create("/reservations/" + reservationInfo.id());
        final ReservationResponse response = new ReservationResponse(reservationInfo);
        return ResponseEntity.created(uri).body(response);
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> findAll(
            @ModelAttribute final ReservationSearchConditionRequest request) {
        final ReservationSearchCondition condition = request.toCondition();
        final List<ReservationInfo> reservationInfos = reservationService.getReservations(condition);
        final List<ReservationResponse> responses = reservationInfos.stream()
                .map(ReservationResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<MemberReservationResponse>> findAllMine(final LoginMemberInfo loginMemberInfo) {
        final List<ReservationInfo> reservationInfos = reservationService.findReservationsByMemberId(
                loginMemberInfo.id());
        final List<MemberReservationResponse> responses = reservationInfos.stream()
                .map(MemberReservationResponse::new)
                .toList();
        return ResponseEntity.ok().body(responses);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") final long id) {
        reservationService.cancelReservationById(id);
        return ResponseEntity.noContent().build();
    }
}
