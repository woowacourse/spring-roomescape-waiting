package roomescape.controller.api;

import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.dto.reservation.AdminReservationCreateRequestDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.service.ReservationService;
import roomescape.service.dto.ReservationCreateDto;

@RestController
@RequestMapping("/admin/reservations")
public class AdminReservationController {

    private final ReservationService reservationService;

    public AdminReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseDto> readReservedReservations() {
        return reservationService.findAllReservedReservations();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDto addReservation(
            @RequestBody AdminReservationCreateRequestDto requestDto) {
        ReservationCreateDto createDto = new ReservationCreateDto(requestDto.date(), requestDto.timeId(),
                requestDto.themeId(), requestDto.memberId());
        return reservationService.createReservation(createDto);
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseDto> searchReservationsByPeriod(
            @RequestParam("themeId") long themeId,
            @RequestParam("memberId") long memberId,
            @RequestParam("dateFrom") LocalDate dateFrom,
            @RequestParam("dateTo") LocalDate dateTo) {
        return reservationService.findReservationBetween(themeId, memberId, dateFrom, dateTo);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(
            @PathVariable("id") final Long id
    ) {
        reservationService.deleteReservation(id);
    }
}
