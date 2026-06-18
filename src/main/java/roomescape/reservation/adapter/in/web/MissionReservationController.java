package roomescape.reservation.adapter.in.web;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.member.domain.AuthenticatedMember;
import roomescape.member.domain.LoginMember;
import roomescape.reservation.application.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.application.port.in.FindReservationUseCase;

@RestController
@RequiredArgsConstructor
public class MissionReservationController {

    private final FindReservationUseCase findReservationUseCase;

    @GetMapping("/reservations-mine")
    public ResponseEntity<ApiResponse<List<ReservationDetailFindResponse>>> findMyReservations(
            @LoginMember AuthenticatedMember member
    ) {
        List<ReservationDetailFindResponse> response = findReservationUseCase.findMyReservations(member.id());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }
}
