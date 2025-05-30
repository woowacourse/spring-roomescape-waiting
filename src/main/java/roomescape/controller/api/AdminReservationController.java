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
import roomescape.dto.reservation.AdminReservationCreateRequestDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.service.dto.ReservationCreateDto;
import roomescape.service.reservation.ReservationCommandService;
import roomescape.service.reservation.ReservationQueryService;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationCommandService reservationCommandService;
    private final ReservationQueryService reservationQueryService;

    public AdminReservationController(ReservationCommandService commandService, ReservationQueryService queryService) {
        this.reservationCommandService = commandService;
        this.reservationQueryService = queryService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponseDto> addReservation(
            @RequestBody AdminReservationCreateRequestDto requestDto) {
        ReservationCreateDto createDto = new ReservationCreateDto(requestDto.date(), requestDto.timeId(),
                requestDto.themeId(), requestDto.memberId());
        ReservationResponseDto responseDto = reservationCommandService.createReservation(createDto);
        return ResponseEntity.created(URI.create("reservations/" + responseDto.id())).body(responseDto);
    }

    @GetMapping("/search")
    public ResponseEntity<List<ReservationResponseDto>> searchReservationsByPeriod(
            @RequestParam("themeId") long themeId,
            @RequestParam("memberId") long memberId,
            @RequestParam("dateFrom") LocalDate dateFrom,
            @RequestParam("dateTo") LocalDate dateTo) {
        List<ReservationResponseDto> reservationBetween = reservationQueryService.findReservationBetween(themeId, memberId,
                dateFrom, dateTo);
        return ResponseEntity.ok(reservationBetween);
    }
}
