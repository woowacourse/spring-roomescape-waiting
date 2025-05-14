package roomescape.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.dto.reservation.AdminReservationCreateRequestDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationCreateDto;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> addReservation(@RequestBody AdminReservationCreateRequestDto requestDto) {
        ReservationCreateDto createDto = new ReservationCreateDto(requestDto.date(), requestDto.timeId(), requestDto.themeId(), requestDto.memberId());
        ReservationResponseDto responseDto = reservationService.createReservation(createDto);
        return ResponseEntity.created(URI.create("reservations/" + responseDto.id())).body(responseDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponseDto>> searchReservationsByPeriod(
            @RequestParam long themeId,
            @RequestParam long memberId,
            @RequestParam LocalDate dateFrom,
            @RequestParam LocalDate dateTo) {
        List<ReservationResponseDto> reservationBetween = reservationService.findReservationBetween(themeId, memberId, dateFrom, dateTo);
        return ResponseEntity.ok(reservationBetween);
    }
}
