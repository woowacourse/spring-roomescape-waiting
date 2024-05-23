package roomescape.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.exceptions.ValidationException;
import roomescape.member.dto.WaitingResponse;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.reservation.service.WaitingService;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin")
public class AdminReservationController {

    private final ReservationService reservationService;
    private final WaitingService waitingService;

    public AdminReservationController(ReservationService reservationService, WaitingService waitingService) {
        this.reservationService = reservationService;
        this.waitingService = waitingService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody AdminReservationRequest adminReservationRequest
    ) {
        ReservationResponse reservationResponse = reservationService.addReservation(adminReservationRequest);

        return ResponseEntity.created(URI.create("/reservations/" + reservationResponse.id()))
                .body(reservationResponse);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> getReservations(
            @RequestParam Long themeId,
            @RequestParam Long memberId,
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo
    ) {
        validateDateRange(dateFrom, dateTo);
        return ResponseEntity.ok(reservationService.searchReservations(themeId, memberId, dateFrom, dateTo));
    }

    private void validateDateRange(LocalDate dateFrom, LocalDate dateTo) {
        if (dateFrom.isAfter(dateTo)) {
            throw new ValidationException(String.format(
                    "검색하려는 시작 날짜가 끝 날짜보다 늦을 수 없습니다. dateFrom = %s, dateTo = %s",
                    dateFrom,
                    dateTo)
            );
        }
    }

    @GetMapping("/waitings")
    public ResponseEntity<List<WaitingResponse>> getWaitings() {
        List<WaitingResponse> waitings = waitingService.findWaitings();
        return ResponseEntity.ok(waitings);
    }
}
