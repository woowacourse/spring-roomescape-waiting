package roomescape.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import roomescape.config.annotation.AuthMember;
import roomescape.controller.dto.request.ReservationRequest;
import roomescape.controller.dto.response.MyReservationResponse;
import roomescape.controller.dto.response.MyWaitingResponse;
import roomescape.controller.dto.response.ReservationResponse;
import roomescape.entity.Member;
import roomescape.service.ReservationService;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Stream;

@RestController
@RequestMapping("/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    private ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReservationResponse createReservation(
            @AuthMember Member member,
            @RequestBody @Valid ReservationRequest request
    ) {
        return ReservationResponse.from(reservationService.addReservation(member, request));
    }

    @PostMapping("/waiting")
    @ResponseStatus(HttpStatus.CREATED)
    public MyWaitingResponse createReservationWaiting(
            @AuthMember Member member,
            @RequestBody @Valid ReservationRequest request
    ) {
        return MyWaitingResponse.from(reservationService.addWaiting(member, request));
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

        List<MyReservationResponse> responseFromReservation = reservationService.findReservationsByMemberId(member).stream()
                .map(MyReservationResponse::fromReservation)
                .toList();

        List<MyReservationResponse> responseFromWaiting = reservationService.findAllWaitingWithRankByMemberId(member).stream()
                .map(MyReservationResponse::fromWaiting)
                .toList();

        return Stream.concat(responseFromReservation.stream(), responseFromWaiting.stream())
                .toList();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReservation(@PathVariable("id") long id) {
        reservationService.removeReservation(id);
    }
}
