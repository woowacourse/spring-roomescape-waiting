package roomescape.reservation.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import roomescape.config.annotation.AuthMember;
import roomescape.member.entity.Member;
import roomescape.reservation.controller.dto.request.ReservationRequest;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.reservation.service.ReservationService;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    private ReservationController(final ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
            @AuthMember Member member,
            @RequestBody @Valid ReservationRequest request
    ) {
        return ReservationResponse.from(
                reservationService.addReservation(
                        member,
                        request.date(),
                        request.themeId(),
                        request.timeId()
                )
        );
    }


    @GetMapping
    public List<ReservationResponse> readReservations() {
        return reservationService.findAllReservations().stream()
                .map(ReservationResponse::from)
                .toList();
    }


    @GetMapping("/filter")
    public List<ReservationResponse> readReservationsByFilter(
        @RequestParam long memberId,
        @RequestParam long themeId,
        @RequestParam LocalDate dateFrom,
        @RequestParam LocalDate dateTo
    ) {
        return reservationService.findReservationsByFilters(themeId, memberId, dateFrom, dateTo)
                .stream()
                .map(ReservationResponse::from)
                .toList();
    }

}
