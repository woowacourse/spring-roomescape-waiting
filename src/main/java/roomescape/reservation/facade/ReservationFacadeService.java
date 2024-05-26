package roomescape.reservation.facade;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.member.domain.Member;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.AdminWaitingResponse;
import roomescape.reservation.dto.ReservationAddRequest;
import roomescape.reservation.dto.ReservationFilterRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.service.ReservationService;
import roomescape.theme.domain.Theme;
import roomescape.theme.service.ThemeService;
import roomescape.time.domain.Time;
import roomescape.time.service.TimeService;

@Service
public class ReservationFacadeService {

    private final ReservationService reservationService;
    private final MemberService memberService;
    private final ThemeService themeService;
    private final TimeService timeService;

    public ReservationFacadeService(ReservationService reservationService, MemberService memberService,
                                    ThemeService themeService, TimeService timeService) {
        this.reservationService = reservationService;
        this.memberService = memberService;
        this.themeService = themeService;
        this.timeService = timeService;
    }

    public ReservationResponse addReservation(ReservationRequest reservationRequest, long memberId) {
        ReservationAddRequest reservationAddRequest = makeReservation(reservationRequest, memberId);

        return ReservationResponse.fromReservation(reservationService.addReservation(reservationAddRequest));
    }

    public ReservationResponse addWaitingReservation(ReservationRequest reservationRequest, long memberId) {
        ReservationAddRequest reservationAddRequest = makeReservation(reservationRequest, memberId);
        Reservation reservation = reservationService.addWaitingReservation(reservationAddRequest, memberId);

        return ReservationResponse.fromReservation(reservation);
    }

    public void addAdminReservation(AdminReservationRequest adminReservationRequest) {
        Time time = timeService.findTime(adminReservationRequest.timeId());
        Theme theme = themeService.findTheme(adminReservationRequest.themeId());
        Member member = memberService.findMember(adminReservationRequest.memberId());

        reservationService.addReservation(
                new ReservationAddRequest(adminReservationRequest.date(), time, theme, member));
    }

    public List<ReservationResponse> findReservations() {
        List<Reservation> reservations = reservationService.findReservationsOrderByDateAndTime();

        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public List<AdminWaitingResponse> findAdminWaitings() {

        List<Reservation> reservations = reservationService.findWaitings();

        return reservations.stream()
                .map(reservation -> new AdminWaitingResponse(reservation.getId(), reservation.getMember().getName(),
                        reservation.getTheme().getName(), reservation.getDate(),
                        reservation.getReservationTime().getStartAt()))
                .toList();
    }

    public List<ReservationTimeAvailabilityResponse> findTimeAvailability(long timeId, LocalDate date) {
        List<Time> allTimes = timeService.findTimesOrderByStartAt();
        List<Time> bookedTimes = reservationService.findBookedTimes(timeId, date);

        return allTimes.stream()
                .map(time -> ReservationTimeAvailabilityResponse.fromTime(time, isTimeBooked(time, bookedTimes)))
                .toList();
    }

    public List<ReservationResponse> findFilteredReservations(ReservationFilterRequest reservationFilterRequest) {
        List<Reservation> reservations = reservationService.findFilteredReservations(reservationFilterRequest);

        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }


    public void removeReservation(long reservationId) {
        reservationService.removeReservation(reservationId);
    }

    public void removeWaitingReservation(long waitingId) {
        reservationService.removeWaitingReservations(waitingId);
    }

    private ReservationAddRequest makeReservation(ReservationRequest reservationRequest, long memberId) {
        Time time = timeService.findTime(reservationRequest.timeId());
        Theme theme = themeService.findTheme(reservationRequest.themeId());
        Member member = memberService.findMember(memberId);

        return new ReservationAddRequest(reservationRequest.date(), time, theme, member);
    }

    private boolean isTimeBooked(Time time, List<Time> bookedTimes) {
        return bookedTimes.contains(time);
    }
}
