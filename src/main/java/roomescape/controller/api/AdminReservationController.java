package roomescape.controller.api;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.reservation.AdminReservationCreateRequest;
import roomescape.dto.reservation.ReservationResponse;
import roomescape.service.ReservationService;
import roomescape.dto.reservation.ReservationCreateRequest;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> addReservation(
            @RequestBody AdminReservationCreateRequest requestDto) {
        ReservationCreateRequest createDto = new ReservationCreateRequest(requestDto.date(), requestDto.themeId(),
                requestDto.timeId(),
                requestDto.memberId());
        ReservationResponse responseDto = reservationService.createReservation(createDto);
        return ResponseEntity.created(URI.create("reservations/" + responseDto.id())).body(responseDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponse>> searchReservationsByPeriod(
            @RequestParam("themeId") long themeId,
            @RequestParam("memberId") long memberId,
            @RequestParam("dateFrom") LocalDate dateFrom,
            @RequestParam("dateTo") LocalDate dateTo) {
        List<ReservationResponse> reservationBetween = reservationService.findReservationBetween(themeId, memberId,
                dateFrom, dateTo);
        return ResponseEntity.ok(reservationBetween);
    }
}
