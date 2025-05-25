package roomescape.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import roomescape.application.service.ReservationTicketService;
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationSearchDto;
import roomescape.dto.request.ReservationTicketRegisterDto;
import roomescape.dto.response.ReservationTicketResponseDto;

@RestController
@RequestMapping("/reservations")
@RequiredArgsConstructor
public class ReservationTicketController {

    private final ReservationTicketService reservationTicketService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationTicketResponseDto> getReservations() {
        return reservationTicketService.getAllReservations();
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationTicketResponseDto> getReservations(
            @ModelAttribute ReservationSearchDto reservationSearchDto) {
        return reservationTicketService.searchReservations(reservationSearchDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationTicketResponseDto addReservation(
            @RequestBody @Valid ReservationTicketRegisterDto reservationTicketRegisterDto, LoginMember loginMember) {
        return reservationTicketService.saveReservation(reservationTicketRegisterDto, loginMember);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable("id") Long id) {
        reservationTicketService.cancelReservation(id);
    }
}
