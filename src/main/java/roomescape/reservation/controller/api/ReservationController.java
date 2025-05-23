package roomescape.reservation.controller.api;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import roomescape.config.annotation.AuthMember;
import roomescape.member.entity.Member;
import roomescape.reservation.controller.dto.request.ReservationRequest;
import roomescape.reservation.controller.dto.response.MyReservationAndWaitingResponse;
import roomescape.reservation.controller.dto.response.ReservationResponse;
import roomescape.waiting.controller.dto.response.WaitingResponse;
import roomescape.reservation.service.ReservationService;

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
    public List<MyReservationAndWaitingResponse> readMyReservations(@AuthMember Member member) {

        List<MyReservationAndWaitingResponse> responseFromReservation = reservationService.findReservationsByMemberId(member).stream()
                .map(MyReservationAndWaitingResponse::fromReservation)
                .toList();

        List<MyReservationAndWaitingResponse> responseFromWaiting = reservationService.findAllWaitingWithRankByMemberId(member).stream()
                .map(MyReservationAndWaitingResponse::fromWaitingWithRank)
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
