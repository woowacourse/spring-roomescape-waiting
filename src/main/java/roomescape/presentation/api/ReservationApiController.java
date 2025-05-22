package roomescape.presentation.api;

import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.auth.AuthRequired;
import roomescape.auth.LoginInfo;
import roomescape.auth.Role;
import roomescape.business.dto.ReservationDto;
import roomescape.business.model.vo.Status;
import roomescape.business.model.vo.UserRole;
import roomescape.business.service.ReservationService;
import roomescape.presentation.dto.request.AdminReservationRequest;
import roomescape.presentation.dto.request.ReservationRequest;
import roomescape.presentation.dto.response.ReservationMineResponse;
import roomescape.presentation.dto.response.ReservationResponse;
import roomescape.presentation.dto.response.ReservationWithAhead;

@RestController
@RequiredArgsConstructor
public class ReservationApiController {

    private final ReservationService reservationService;

    @PostMapping("/reservations")
    @AuthRequired
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid ReservationRequest request,
                                                                 LoginInfo loginInfo) {
        ReservationDto reservationDto = reservationService.addAndGet(request.date(), request.timeId(),
                request.themeId(), loginInfo.id(), request.status());
        ReservationResponse response = ReservationResponse.from(reservationDto);
        return ResponseEntity.created(URI.create("/reservations")).body(response);
    }

    @PostMapping("/admin/reservations")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public ResponseEntity<ReservationResponse> adminCreateReservation(
            @RequestBody @Valid AdminReservationRequest request) {
        ReservationDto reservationDto = reservationService.addAndGet(request.date(), request.timeId(),
                request.themeId(), request.userId(),
                Status.RESERVED);
        ReservationResponse response = ReservationResponse.from(reservationDto);
        return ResponseEntity.created(URI.create("/reservations")).body(response);
    }

    @GetMapping("/admin/reservations/waiting")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public ResponseEntity<List<ReservationResponse>> getWaitingReservations() {
        List<ReservationResponse> responses = reservationService.getAllWaitingReservations();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reservations")
    @AuthRequired
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @RequestParam(required = false) String themeId,
            @RequestParam(required = false, name = "memberId") String userId,
            @RequestParam(required = false) LocalDate dateFrom,
            @RequestParam(required = false) LocalDate dateTo
    ) {
        List<ReservationDto> reservationDtos = reservationService.getAll(themeId, userId, dateFrom, dateTo);
        List<ReservationResponse> responses = ReservationResponse.from(reservationDtos);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/reservations/mine")
    @AuthRequired
    public ResponseEntity<List<ReservationMineResponse>> getMyReservations(LoginInfo loginInfo) {
        List<ReservationWithAhead> myReservationsWithAhead = reservationService.getMyReservations(loginInfo.id());
        List<ReservationMineResponse> responses = ReservationMineResponse.from(myReservationsWithAhead);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/reservations/{id}")
    @AuthRequired
    @Role(UserRole.ADMIN)
    public ResponseEntity<Void> deleteReservation(@PathVariable String id) {
        reservationService.deleteAndUpdateWaiting(id);
        return ResponseEntity.noContent().build();
    }
}
