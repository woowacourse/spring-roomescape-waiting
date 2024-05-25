package roomescape.reservation.presentation;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Qualifier;
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
import roomescape.reservation.application.ReservationManageService;
import roomescape.reservation.application.BookingQueryService;
import roomescape.reservation.application.ReservationTimeService;
import roomescape.reservation.application.ThemeService;
import roomescape.reservation.application.WaitingQueryService;
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
    private final BookingQueryService bookingQueryService;
    private final ReservationManageService waitingScheduler;
    private final ReservationManageService bookingScheduler;
    private final WaitingQueryService waitingQueryService;
    private final ReservationTimeService reservationTimeService;
    private final ThemeService themeService;

    public ReservationController(BookingQueryService bookingQueryService,
                                 @Qualifier("waitingManageService") ReservationManageService waitingScheduler,
                                 @Qualifier("bookingManageService") ReservationManageService bookingScheduler,
                                 WaitingQueryService waitingQueryService,
                                 ReservationTimeService reservationTimeService,
                                 ThemeService themeService) {
        this.bookingQueryService = bookingQueryService;
        this.waitingScheduler = waitingScheduler;
        this.bookingScheduler = bookingScheduler;
        this.waitingQueryService = waitingQueryService;
        this.reservationTimeService = reservationTimeService;
        this.themeService = themeService;
    }

    @PostMapping
    public ResponseEntity<ReservationResponse> createReservation(@RequestBody @Valid ReservationSaveRequest request,
                                                                 Member loginMember) {
        Reservation newReservation = toNewReservation(request, loginMember);
        if (newReservation.isBooking()) {
            Reservation createdReservation = bookingScheduler.create(newReservation);
            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(ReservationResponse.from(createdReservation));
        }
        Reservation createdReservation = waitingScheduler.create(newReservation);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ReservationResponse.from(createdReservation));
    }

    private Reservation toNewReservation(ReservationSaveRequest request, Member loginMember) {
        ReservationTime reservationTime = reservationTimeService.findById(request.timeId());
        Theme theme = themeService.findById(request.themeId());
        return request.toModel(theme, reservationTime, loginMember);
    }

    @GetMapping("/mine")
    public ResponseEntity<List<MyReservationResponse>> findMyReservations(Member loginMember) {
        List<MyReservationResponse> myBookingResponses = bookingQueryService.findAllByMember(loginMember)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
        List<MyReservationResponse> myWaitingResponses = waitingQueryService.findAllWithPreviousCountByMember(loginMember)
                .stream()
                .map(MyReservationResponse::from)
                .toList();
        List<MyReservationResponse> myReservationResponses = new ArrayList<>(myBookingResponses);
        myReservationResponses.addAll(myWaitingResponses);
        return ResponseEntity.ok(myReservationResponses);
    }

    @DeleteMapping("/{id}/waiting")
    public ResponseEntity<Void> deleteMyWaitingReservation(@PathVariable Long id, Member loginMember) {
        waitingScheduler.delete(id, loginMember);
        return ResponseEntity.noContent().build();
    }
}
