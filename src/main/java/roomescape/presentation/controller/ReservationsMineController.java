package roomescape.presentation.controller;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.business.service.ReservationService;
import roomescape.config.AuthenticationPrincipal;
import roomescape.presentation.dto.LoginMember;
import roomescape.presentation.dto.ReservationMineResponse;

@RestController
@RequestMapping("/reservations-mine")
public class ReservationsMineController {

    private final ReservationService reservationService;

    public ReservationsMineController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    public ResponseEntity<List<ReservationMineResponse>> readReservationByMember(
            @AuthenticationPrincipal final LoginMember loginMember
    ) {
        final List<ReservationMineResponse> reservationMineResponses =
                reservationService.findByMemberId(loginMember.id());

        return ResponseEntity.ok(reservationMineResponses);
    }

    // TODO: 통합 테스트 추가, 삭제 성공 시나리오, waitInfoId, memberId가 없는 경우 실패 시나리오
    @DeleteMapping("/{waitInfoId}")
    public ResponseEntity<List<ReservationMineResponse>> deleteReservation(
            @AuthenticationPrincipal final LoginMember loginMember,
            @PathVariable("waitInfoId") final Long waitInfoId
    ) {
        reservationService.deleteWaitInfoByIdAndMemberId(waitInfoId, loginMember.id());

        return ResponseEntity.noContent()
                .build();
    }
}
