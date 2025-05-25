package roomescape.reservation.controller;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.auth.annotation.RequireRole;
import roomescape.global.auth.dto.UserInfo;
import roomescape.member.domain.MemberRole;
import roomescape.reservation.dto.request.ReservationCreateRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationCompositeService;
import roomescape.reservation.service.WaitingModuleService;

@RestController
public class WaitingController {

    private final WaitingModuleService waitingModuleService;
    private final ReservationCompositeService reservationCompositeService;

    public WaitingController(final WaitingModuleService waitingModuleService,
                             ReservationCompositeService reservationCompositeService) {
        this.waitingModuleService = waitingModuleService;
        this.reservationCompositeService = reservationCompositeService;
    }

    @RequireRole(MemberRole.USER)
    @PostMapping("/waiting")
    public ResponseEntity<ReservationResponse> createWaiting(
            @RequestBody ReservationCreateRequest request,
            UserInfo userInfo
    ) {
        ReservationResponse dto = reservationCompositeService.createWaiting(request.date(), request.timeId(), request.themeId(),
                userInfo.id(), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    @GetMapping("/waiting")
    public ResponseEntity<List<ReservationResponse>> findWaiting(
    ) {
        return ResponseEntity.ok(waitingModuleService.findWaitings());
    }

    @RequireRole(MemberRole.USER)
    @DeleteMapping("/waiting/{id}")
    public ResponseEntity<Void> deleteReservations(
            @PathVariable("id") Long id
    ) {
        waitingModuleService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
