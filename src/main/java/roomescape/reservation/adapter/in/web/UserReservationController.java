package roomescape.reservation.adapter.in.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.common.api.ApiResponse;
import roomescape.member.domain.AuthenticatedMember;
import roomescape.member.domain.LoginMember;
import roomescape.reservation.application.port.in.CancelReservationUseCase;
import roomescape.reservation.application.port.in.CreateReservationUseCase;
import roomescape.reservation.application.port.in.FindReservationUseCase;
import roomescape.reservation.application.port.in.CreateReservationUseCase;
import roomescape.reservation.application.port.in.FindReservationUseCase;
import roomescape.reservation.application.dto.request.ReservationSaveRequest;
import roomescape.reservation.application.dto.response.ReservationDetailFindResponse;
import roomescape.reservation.application.dto.response.ReservationSaveResponse;

import java.util.List;

@RestController
@RequestMapping("/api/user/reservations")
@RequiredArgsConstructor
@Validated
public class UserReservationController {

    private final CreateReservationUseCase createReservationUseCase;
    private final CancelReservationUseCase cancelReservationUseCase;
    private final FindReservationUseCase findReservationUseCase;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationSaveResponse>> save(
            @RequestBody @Valid ReservationSaveRequest body,
            @LoginMember AuthenticatedMember member
    ) {
        ReservationSaveResponse response = createReservationUseCase.save(body, member.id());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteByUser(
            @PathVariable @Positive long id,
            @LoginMember AuthenticatedMember member
    ) {
        cancelReservationUseCase.deleteByIdForUser(id, member.id());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ReservationDetailFindResponse>>> findMyReservations(
            @LoginMember AuthenticatedMember member
    ) {
        List<ReservationDetailFindResponse> response = findReservationUseCase.findMyReservations(member.id());
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(response));
    }

}
