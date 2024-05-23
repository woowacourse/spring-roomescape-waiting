package roomescape.domain.reservation.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import roomescape.domain.login.controller.MemberResolver;
import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.Reservation;
import roomescape.domain.reservation.dto.*;
import roomescape.domain.reservation.service.ReservationService;
import roomescape.domain.time.dto.BookableTimeResponse;
import roomescape.domain.time.dto.BookableTimesRequest;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    @GetMapping("/reservations")
    public ResponseEntity<List<ReservationResponse>> getReservationList() {
        List<Reservation> reservations = reservationService.findAllReservation();
        List<ReservationResponse> reservationResponses = ReservationResponse.fromList(reservations);
        return ResponseEntity.ok(reservationResponses);
    }

    @GetMapping("/reservations/search")
    public ResponseEntity<List<ReservationResponse>> getConditionalReservationList(
            @ModelAttribute ReservationFindRequest reservationFindRequest) {
        List<Reservation> reservations = reservationService.findFilteredReservationList(
                reservationFindRequest.themeId(),
                reservationFindRequest.memberId(),
                reservationFindRequest.dateFrom(),
                reservationFindRequest.dateTo()
        );
        List<ReservationResponse> reservationResponses = ReservationResponse.fromList(reservations);
        return ResponseEntity.ok(reservationResponses);
    }

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody ReservationAddRequest reservationAddRequest,
                                                              @MemberResolver Member member) {
        reservationAddRequest = new ReservationAddRequest(
                reservationAddRequest.date(),
                reservationAddRequest.timeId(),
                reservationAddRequest.themeId(),
                member.getId()
        );
        Reservation reservation = reservationService.addReservation(reservationAddRequest);
        ReservationResponse reservationResponse = ReservationResponse.from(reservation);
        return ResponseEntity.created(URI.create("/reservation/" + reservation.getId())).body(reservationResponse);
    }

    @GetMapping("/bookable-times")
    public ResponseEntity<List<BookableTimeResponse>> getTimesWithStatus(
            @RequestParam("date") LocalDate date,
            @RequestParam("themeId") Long themeId) {
        return ResponseEntity.ok(reservationService.findBookableTimes(new BookableTimesRequest(date, themeId)));
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationMineResponse>> findSpecificMemberReservation(@MemberResolver Member member) {
        return ResponseEntity.ok(reservationService.findReservationByMemberId(member.getId()));
    }

    @PostMapping("/reservations/wait")
    public ResponseEntity<ReservationResponse> addReservationWait(@RequestBody ReservationWaitAddRequest reservationWaitAddRequest,
                                                                  @MemberResolver Member member) {
        reservationWaitAddRequest = new ReservationWaitAddRequest(
                reservationWaitAddRequest.date(),
                reservationWaitAddRequest.timeId(),
                reservationWaitAddRequest.themeId(),
                member.getId()
        );
        Reservation reservation = reservationService.addReservationWait(reservationWaitAddRequest);
        ReservationResponse reservationResponse = ReservationResponse.from(reservation);
        return ResponseEntity.created(URI.create("/reservation/" + reservation.getId())).body(reservationResponse);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> removeReservationWait(@PathVariable("id") Long id) {
        reservationService.removeReservationWait(id);
        return ResponseEntity.noContent().build();
    }
}
