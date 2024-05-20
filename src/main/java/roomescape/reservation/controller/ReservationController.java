package roomescape.reservation.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.domain.AuthInfo;
import roomescape.global.annotation.LoginUser;
import roomescape.reservation.controller.dto.MyReservationResponse;
import roomescape.reservation.controller.dto.ReservationQueryRequest;
import roomescape.reservation.controller.dto.ReservationRequest;
import roomescape.reservation.controller.dto.ReservationResponse;
import roomescape.reservation.controller.dto.WaitingRequest;
import roomescape.reservation.service.MemberReservationService;
import roomescape.reservation.service.dto.MemberReservationCreate;
import roomescape.reservation.service.dto.WaitingCreate;
import roomescape.reservation.service.WaitingReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final MemberReservationService memberReservationService;
    private final WaitingReservationService waitingReservationService;

    public ReservationController(MemberReservationService memberReservationService, WaitingReservationService waitingReservationService) {
        this.memberReservationService = memberReservationService;
        this.waitingReservationService = waitingReservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationResponse>> reservations(
            @RequestParam(value = "themeId", required = false) Long themeId,
            @RequestParam(value = "memberId", required = false) Long memberId,
            @RequestParam(value = "dateFrom", required = false) LocalDate startDate,
            @RequestParam(value = "dateTo", required = false) LocalDate endDate
    ) {
        return ResponseEntity.ok(memberReservationService.findMemberReservations(
                new ReservationQueryRequest(themeId, memberId, startDate, endDate)));
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> create(@LoginUser AuthInfo authInfo,
                                                      @RequestBody @Valid ReservationRequest reservationRequest) {
        MemberReservationCreate memberReservationCreate = new MemberReservationCreate(
                authInfo.getId(),
                reservationRequest.date(),
                reservationRequest.timeId(),
                reservationRequest.themeId()
        );
        ReservationResponse response = memberReservationService.createMemberReservation(memberReservationCreate);
        return ResponseEntity.created(URI.create("/reservations/" + response.memberReservationId())).body(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@LoginUser AuthInfo authInfo,
                                       @PathVariable("id") @Min(1) long reservationMemberId) {
        memberReservationService.deleteMemberReservation(authInfo, reservationMemberId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    public ResponseEntity<List<MyReservationResponse>> getMyReservations(@LoginUser AuthInfo authInfo) {
        List<MyReservationResponse> responses = memberReservationService.findMyReservations(authInfo)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationResponse>> getWaiting() {
        return ResponseEntity.ok().body(waitingReservationService.getWaiting());
    }

    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> addWaiting(@LoginUser AuthInfo authInfo,
                                                          @RequestBody @Valid WaitingRequest waitingRequest) {
        WaitingCreate waitingCreate = new WaitingCreate(
                authInfo.getId(),
                waitingRequest.date(),
                waitingRequest.timeId(),
                waitingRequest.themeId()
        );
        ReservationResponse reservationResponse = waitingReservationService.addWaiting(waitingCreate);
        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.memberReservationId()))
                .body(reservationResponse);
    }

    @DeleteMapping("/{id}/waiting")
    public ResponseEntity<Void> deleteWaiting(@LoginUser AuthInfo authInfo,
                                              @PathVariable("id") @Min(1) long reservationMemberId) {
        waitingReservationService.deleteWaiting(authInfo, reservationMemberId);
        return ResponseEntity.noContent().build();
    }
}
