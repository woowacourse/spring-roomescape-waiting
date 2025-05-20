package roomescape.controller.api;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
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
import roomescape.config.annotation.AuthMember;
import roomescape.controller.dto.request.ReservationRequest;
import roomescape.controller.dto.response.MyReservationResponse;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.entity.Member;
import roomescape.service.ReservationService;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    private ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
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

    @GetMapping("/mine")
    public List<MyReservationResponse> readMyReservations(@AuthMember Member member) {
        return reservationService.findReservationsByMemberId(member).stream()
            .map(MyReservationResponse::from)
            .toList();
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
        @AuthMember Member member,
        @RequestBody @Valid ReservationRequest request
    ) {
        return ReservationResponse.from(reservationService.addReservationAfterNow(member, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable("id") long id) {
        reservationService.removeReservation(id);
    }
}
