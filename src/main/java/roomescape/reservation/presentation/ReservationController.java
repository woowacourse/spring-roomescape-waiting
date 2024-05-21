package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import roomescape.member.domain.Member;
import roomescape.reservation.application.ReservationService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.request.ReservationSaveRequest;
import roomescape.reservation.dto.response.MyReservationResponse;
import roomescape.reservation.dto.response.ReservationResponse;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/reservations")
public class ReservationController {
    private final ReservationService reservationService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationController(ReservationService reservationService,
                                 ReservationTimeService reservationTimeService,
                                 ThemeService themeService) {
        this.reservationService = reservationService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid ReservationSaveRequest request,
                                                                 Member loginMember) {
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());
        Reservation newReservation = request.toModel(theme, reservationTime, loginMember);
        Reservation createReservation = reservationService.create(newReservation);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(createReservation));
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(Member loginMember) {
        List<MyReservationResponse> myBookingResponses = reservationService.findReservationsInBookingByMember(loginMember)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
        List<MyReservationResponse> myWaitingResponses = reservationService.findWaitingReservationsWithPreviousCountByMember(loginMember)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
        List<MyReservationResponse> myReservationResponses = new ArrayList<>(myBookingResponses);
        myReservationResponses.addAll(myWaitingResponses);
        return ResponseEntity.ok(myReservationResponses);
    }

    @DeleteMapping("/{id}/waiting")
    public ResponseEntity<Void> deleteMyWaitingReservation(@PathVariable Long id, Member loginMember) {
        reservationService.deleteWaitingReservationByMember(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
