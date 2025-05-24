package roomescape.controller.admin;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reserveticket.ReserveTicketWaiting;
import roomescape.dto.admin.AdminReservationAddDto;
import roomescape.dto.reservation.AddReservationDto;
import roomescape.dto.reservation.ReservationResponseDto;
import roomescape.dto.reservationmember.ReservationTicketWaitingDto;
import roomescape.service.reserveticket.ReserveTicketService;

@RestController
@RequestMapping("/admin")
public class AdminController {

    private final ReserveTicketService reserveTicketService;

    public AdminController(ReserveTicketService reserveTicketService) {
        this.reserveTicketService = reserveTicketService;
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponseDto> addReservations(
            @RequestBody @Valid AdminReservationAddDto newReservationDto) {
        AddReservationDto addReservationDto = new AddReservationDto(newReservationDto.date(),
                newReservationDto.timeId(),
                newReservationDto.themeId());

        long id = reserveTicketService.addReservation(addReservationDto,
                newReservationDto.memberId());
        Reservation reservation = reserveTicketService.getReservationById(id);

        ReservationResponseDto reservationResponseDto = new ReservationResponseDto(reservation.getId(),
                reservation.getStartAt(), reservation.getDate(), reservation.getThemeName());
        return ResponseEntity.created(URI.create("/reservations/" + id)).body(reservationResponseDto);
    }

    @GetMapping("/reservations/waiting")
    public ResponseEntity<List<ReservationTicketWaitingDto>> reservationResponseDtoResponseEntity() {
        List<ReserveTicketWaiting> reserveTickets = reserveTicketService.allReservationTickets();

        List<ReservationTicketWaitingDto> reservationDtos = reserveTickets.stream()
                .filter(reserveTicketWaiting -> reserveTicketWaiting.getReservationStatus()
                        .equals(ReservationStatus.PREPARE))
                .map((reserveTicket) -> new ReservationTicketWaitingDto(reserveTicket.getId(),
                        reserveTicket.getName(),
                        reserveTicket.getThemeName(),
                        reserveTicket.getDate(),
                        reserveTicket.getStartAt(),
                        reserveTicket.getReservationStatus().getStatus(),
                        reserveTicket.getWaitRank()))
                .toList();

        return ResponseEntity.ok(reservationDtos);
    }


    @PostMapping("/reservations/waiting")
    public ResponseEntity<ReservationResponseDto> addWaitingReservationn(
            @RequestBody @Valid AdminReservationAddDto newReservationDto) {

        AddReservationDto addReservationDto = new AddReservationDto(newReservationDto.date(),
                newReservationDto.timeId(),
                newReservationDto.themeId());

        long id = reserveTicketService.addReservation(addReservationDto,
                newReservationDto.memberId());
        Reservation reservation = reserveTicketService.getReservationById(id);

        ReservationResponseDto reservationResponseDto = new ReservationResponseDto(reservation.getId(),
                reservation.getStartAt(), reservation.getDate(), reservation.getThemeName());
        return ResponseEntity.created(URI.create("/reservations/" + id)).body(reservationResponseDto);
    }


    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> deleteReservations(@PathVariable Long id) {
        reserveTicketService.deleteReservation(id);
        return ResponseEntity.noContent().build();
    }
}


