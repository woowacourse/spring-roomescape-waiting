package roomescape.domain.reservation.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import roomescape.domain.login.controller.MemberResolver;
import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.dto.request.BookableTimesRequest;
import roomescape.domain.reservation.dto.request.ReservationAddRequest;
import roomescape.domain.reservation.dto.response.BookableTimeResponse;
import roomescape.domain.reservation.dto.response.ReservationMineResponse;
import roomescape.domain.reservation.dto.response.ReservationResponse;
import roomescape.domain.reservation.service.ReservationService;

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

    @PostMapping("/reservations")
    public ResponseEntity<ReservationResponse> addReservation(@RequestBody ReservationAddRequest reservationAddRequest,
                                                              @MemberResolver Member member,
                                                              @RequestParam(name = "waiting", required = false, defaultValue = "false") boolean waiting) {
        reservationAddRequest = reservationAddRequest.addMemberId(member.getId());
        Reservation reservation;
        if (waiting) {
            reservation = reservationService.addWaitingReservation(reservationAddRequest);
        } else {
            reservation = reservationService.addReservation(reservationAddRequest);
        }
        ReservationResponse reservationResponse = ReservationResponse.from(reservation);
        return ResponseEntity.created(URI.create("/reservations/" + reservation.getId())).body(reservationResponse);
    }

    @DeleteMapping("/reservations/{id}")
    public ResponseEntity<Void> removeReservation(@PathVariable("id") Long id) {
        reservationService.removeReservation(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/bookable-times")
    public ResponseEntity<List<BookableTimeResponse>> getTimesWithStatus(@RequestParam("date") LocalDate date,
                                                                         @RequestParam("themeId") Long themeId) {
        return ResponseEntity.ok(reservationService.findBookableTimes(new BookableTimesRequest(date, themeId)));
    }

    @GetMapping("/reservations-mine")
    public ResponseEntity<List<ReservationMineResponse>> findSpecificMemberReservation(@MemberResolver Member member) {
        return ResponseEntity.ok(reservationService.findReservationByMemberId(member.getId()));
    }
}
