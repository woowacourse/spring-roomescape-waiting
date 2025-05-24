package roomescape.controller.admin;

import jakarta.validation.Valid;
import java.net.URI;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.Reservation;
import roomescape.dto.admin.AdminReservationAddDto;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.service.reservation.ReservationService;
import roomescape.service.reserveticket.ReserveTicketService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private static final String ADMIN_CREATED_MESSAGE = "어드민이 생성한 예약입니다.";

    private final ReservationService reservationService;
    private final ReserveTicketService reserveTicketService;

    public AdminController(ReservationService reservationService, ReserveTicketService reserveTicketService) {
        this.reservationService = reservationService;
        this.reserveTicketService = reserveTicketService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponseDto> addReservations(
            @RequestBody @Valid AdminReservationAddDto newReservationDto) {
        AddReservationDto addReservationDto = new AddReservationDto(newReservationDto.date(),
                newReservationDto.timeId(),
                newReservationDto.themeId());

        long id = reserveTicketService.addReservation(addReservationDto, newReservationDto.memberId());
        Reservation reservation = reservationService.getReservationById(id);

        ReservationResponseDto reservationResponseDto = new ReservationResponseDto(reservation.getId(),
                reservation.getName(), reservation.getStartAt(), reservation.getDate(), reservation.getThemeName());
        return ResponseEntity.created(URI.create("/reservations/" + id)).body(reservationResponseDto);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservations(@PathVariable Long id) {
        reserveTicketService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}
