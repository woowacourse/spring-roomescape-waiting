package roomescape.presentation.controller;

import jakarta.validation.Valid;
import java.util.List;
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
import roomescape.dto.LoginMember;
import roomescape.dto.request.ReservationRegisterDto;
import roomescape.dto.request.ReservationSearchDto;
import roomescape.dto.response.ReservationResponseDto;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseDto> getReservations() {
        return reservationService.getAllReservations();
    }

    @GetMapping("/search")
    @ResponseStatus(HttpStatus.OK)
    public List<ReservationResponseDto> getReservations(
            @ModelAttribute ReservationSearchDto reservationSearchDto) {
        return reservationService.searchReservations(reservationSearchDto);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponseDto addReservation(
            @RequestBody @Valid ReservationRegisterDto reservationRegisterDto, LoginMember loginMember) {
        return reservationService.saveReservation(reservationRegisterDto, loginMember);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable("id") Long id) {
        reservationService.cancelReservation(id);
    }
}
