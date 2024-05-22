package roomescape.reservation.controller;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.dto.LoginMember;
import roomescape.common.dto.MultipleResponses;
import roomescape.reservation.dto.MemberReservationResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationSaveRequest;
import roomescape.reservation.dto.ReservationSearchConditionRequest;
import roomescape.reservation.dto.ReservationWaitingResponse;
import roomescape.reservation.service.ReservationService;

@RestController
public class ReservationApiController {

    private final ReservationService reservationService;

    public ReservationApiController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<MultipleResponses<ReservationResponse>> findAll(@RequestParam(defaultValue = "SUCCESS") String status) {
        MultipleResponses<ReservationResponse> reservationResponses = reservationService.findAllByStatus(status);

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/mine")
    public ResponseEntity<MultipleResponses<MemberReservationResponse>> findMemberReservations(LoginMember loginMember) {
        MultipleResponses<MemberReservationResponse> memberReservationResponses = reservationService.findMemberReservations(loginMember);

        return ResponseEntity.ok(memberReservationResponses);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<MultipleResponses<ReservationResponse>> findAllBySearchCond(
            @Valid @ModelAttribute ReservationSearchConditionRequest reservationSearchConditionRequest
    ) {
        MultipleResponses<ReservationResponse> reservationResponses = reservationService.findAllBySearchCondition(reservationSearchConditionRequest);

        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/admin/reservations/waiting")
    public ResponseEntity<MultipleResponses<ReservationWaitingResponse>> findWaitingReservations() {
        MultipleResponses<ReservationWaitingResponse> waitingReservations = reservationService.findWaitingReservations();

        return ResponseEntity.ok(waitingReservations);
    }

    @PostMapping(path = {"/reservations", "/admin/reservations"})
    public ResponseEntity<ReservationResponse> saveReservation(
            @Valid @RequestBody ReservationSaveRequest reservationSaveRequest,
            LoginMember loginMember
    ) {
        ReservationResponse reservationResponse = reservationService.saveReservationSuccess(reservationSaveRequest, loginMember);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id())).body(reservationResponse);
    }

    @PostMapping("/reservations/waiting")
    public ResponseEntity<ReservationResponse> saveReservationWaiting(
            @Valid @RequestBody ReservationSaveRequest reservationSaveRequest,
            LoginMember loginMember
    ) {
        ReservationResponse reservationResponse = reservationService.saveReservationWaiting(reservationSaveRequest, loginMember);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id())).body(reservationResponse);
    }

    @PostMapping("/reservations/{id}")
    public ResponseEntity<Void> cancel(@PathVariable("id") Long id) {
        reservationService.cancelById(id);

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
