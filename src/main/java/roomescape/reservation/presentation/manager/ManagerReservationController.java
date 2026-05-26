package roomescape.reservation.presentation.manager;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.member.AuthenticatedMember;
import roomescape.member.LoginMember;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.dto.response.ReservationSaveResponse;

import java.util.List;

@RestController
@RequestMapping("/api/manager")
@RequiredArgsConstructor
@Validated
public class ManagerReservationController {

    private final ReservationService reservationService;

    @GetMapping("/stores/{storeId}/reservations")
    public ResponseEntity<ApiResponse<List<ReservationDetailFindResponse>>> findStoreReservationDetails(
            @PathVariable @Positive long storeId,
            @LoginMember AuthenticatedMember member
    ) {
        List<ReservationDetailFindResponse> responses = reservationService.findStoreReservationDetails(member.id(), storeId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responses));
    }

    @DeleteMapping("/stores/{storeId}/reservations/{reservationId}")
    public ResponseEntity<ApiResponse<Void>> deleteByManager(
            @PathVariable @Positive long reservationId,
            @PathVariable @Positive long storeId,
            @LoginMember AuthenticatedMember member
    ) {
        reservationService.deleteById(reservationId, member.id(), storeId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }

    @PatchMapping("/stores/{storeId}/reservations/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationSaveResponse>> update(
            @RequestBody @Valid ReservationUpdateRequest request,
            @PathVariable @Positive long reservationId,
            @PathVariable @Positive long storeId,
            @LoginMember AuthenticatedMember member
    ) {
        ReservationSaveResponse response = reservationService.update(request, reservationId, member.id(), storeId);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }
}
