package roomescape.reservation.presentation.user;

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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.member.AuthenticatedMember;
import roomescape.member.LoginMember;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.request.ReservationUpdateRequest;
import roomescape.reservation.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.dto.response.ReservationSaveResponse;

import java.util.List;

@RestController
@RequestMapping("/api/user/reservations")
@RequiredArgsConstructor
@Validated
public class UserReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationSaveResponse>> save(
            @RequestBody @Valid ReservationSaveRequest body,
            @LoginMember AuthenticatedMember member
    ) {
        ReservationSaveResponse response = reservationService.save(body, member.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteByUser(
            @PathVariable @Positive long id,
            @LoginMember AuthenticatedMember member
    ) {
        reservationService.deleteByIdForUser(id, member.id());
        return ResponseEntity.status(HttpStatus.NO_CONTENT).body(ApiResponse.success(null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ReservationDetailFindResponse>>> findMyReservations(
            @LoginMember AuthenticatedMember member
    ) {
        List<ReservationDetailFindResponse> response = reservationService.findMyReservations(member.id());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

    @PatchMapping("/{reservationId}")
    public ResponseEntity<ApiResponse<ReservationSaveResponse>> update(
            @RequestBody @Valid ReservationUpdateRequest request,
            @PathVariable @Positive long reservationId,
            @LoginMember AuthenticatedMember member
    ) {
        ReservationSaveResponse response = reservationService.updateForUser(request, reservationId, member.id());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }
}
