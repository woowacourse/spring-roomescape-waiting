package roomescape.reservation.controller;

import static roomescape.reservation.controller.response.ReservationSuccessCode.RESERVE;
import static roomescape.reservation.controller.response.ReservationSuccessCode.SEARCH_RESERVATION;

import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.global.response.ApiResponse;
import roomescape.global.response.PageResponse;
import roomescape.reservation.controller.request.ReserveByAdminRequest;
import roomescape.reservation.controller.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.ReservedQueryService;
import roomescape.reservation.service.command.ReserveCommand;

@RequiredArgsConstructor
@RestController
@RequestMapping("/admin/reservations")
public class ReservationAdminApiController {

    private final ReservationService reservationService;
    private final ReservedQueryService reservedQueryService;

    @PostMapping
    public ResponseEntity<ApiResponse<ReservationResponse>> reserve(
            @RequestBody @Valid ReserveByAdminRequest request
    ) {
        ReservationResponse response = reservationService.reserve(
                ReserveCommand.byAdmin(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(RESERVE, response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<ReservationResponse>>> searchReservations(
            @RequestParam(required = false) Long themeId,
            @RequestParam(required = false) Long memberId,
            @RequestParam(required = false) LocalDate from,
            @RequestParam(required = false) LocalDate to,
            Pageable pageable
    ) {
        Page<ReservationResponse> responses = reservedQueryService.getFilteredReserved(themeId, memberId, from,
                to, pageable);

        PageResponse<ReservationResponse> pageResponse = PageResponse.from(responses);

        return ResponseEntity.ok(
                ApiResponse.success(SEARCH_RESERVATION, pageResponse));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReservation(@PathVariable Long id) {
        reservationService.delete(id);

        return ResponseEntity.noContent().build();
    }
}
