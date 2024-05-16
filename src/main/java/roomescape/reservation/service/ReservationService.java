package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.admin.domain.FilterInfo;
import roomescape.admin.dto.AdminReservationRequest;
import roomescape.admin.dto.ReservationFilterRequest;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.domain.Member;
import roomescape.member.exception.MemberExceptionCode;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.reservation.dto.ReservationWaitingResponse;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.theme.domain.Theme;
import roomescape.theme.exception.ThemeExceptionCode;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.Time;
import roomescape.time.exception.TimeExceptionCode;
import roomescape.time.repository.TimeRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final TimeRepository timeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(ReservationRepository reservationRepository, TimeRepository timeRepository,
                              ThemeRepository themeRepository, MemberRepository memberRepository) {
        this.reservationRepository = reservationRepository;
        this.timeRepository = timeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public ReservationResponse addReservation(ReservationRequest reservationRequest, long memberId) {
        Reservation reservation = reservationRequest.fromRequest(memberId);

        Time time = timeRepository.findById(reservation.getReservationTime().getId())
                .orElseThrow(() -> new RoomEscapeException(TimeExceptionCode.FOUND_TIME_IS_NULL_EXCEPTION));
        Theme theme = themeRepository.findById(reservation.getTheme().getId())
                .orElseThrow(() -> new RoomEscapeException(ThemeExceptionCode.FOUND_THEME_IS_NULL_EXCEPTION));

        Reservation saveReservation = Reservation.of(reservation.getDate(), time,
                theme, reservation.getMember());
        return ReservationResponse.fromReservation(reservationRepository.save(saveReservation));
    }

    public void addAdminReservation(AdminReservationRequest adminReservationRequest) {
        Reservation reservation = adminReservationRequest.fromRequest();

        Time time = timeRepository.findById(reservation.getReservationTime().getId())
                .orElseThrow(() -> new RoomEscapeException(TimeExceptionCode.FOUND_TIME_IS_NULL_EXCEPTION));
        Theme theme = themeRepository.findById(reservation.getTheme().getId())
                .orElseThrow(() -> new RoomEscapeException(ThemeExceptionCode.FOUND_THEME_IS_NULL_EXCEPTION));
        Member member = memberRepository.findMemberById(adminReservationRequest.memberId())
                .orElseThrow(() -> new RoomEscapeException(MemberExceptionCode.MEMBER_NOT_EXIST_EXCEPTION));

        Reservation saveReservation = Reservation.of(reservation.getDate(), time, theme, member);
        ReservationResponse.fromReservation(reservationRepository.save(saveReservation));
    }


    public List<ReservationResponse> findReservations() {
        List<Reservation> reservations = reservationRepository.findAllByOrderByDateAscTimeAsc();

        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public List<ReservationTimeAvailabilityResponse> findTimeAvailability(long themeId, LocalDate date) {
        List<Time> allTimes = timeRepository.findAllByOrderByStartAt();
        List<Reservation> reservations = reservationRepository.findAllByThemeIdAndDate_Date(themeId, date);
        List<Time> bookedTimes = extractReservationTimes(reservations);

        return allTimes.stream()
                .map(time -> ReservationTimeAvailabilityResponse.fromTime(time, isTimeBooked(time, bookedTimes)))
                .toList();
    }

    public List<ReservationResponse> findFilteredReservations(ReservationFilterRequest reservationFilterRequest) {
        FilterInfo filterInfo = reservationFilterRequest.toFilterInfo();

        return reservationRepository.findAllByMemberIdAndThemeIdAndDate_DateBetween(filterInfo.getMemberId(),
                        filterInfo.getThemeId(), filterInfo.getFromDate(), filterInfo.getToDate()).stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public List<ReservationWaitingResponse> findMemberReservations(long id) {
        List<Reservation> reservations = reservationRepository.findAllByMemberId(id);

        return reservations.stream()
                .map(ReservationWaitingResponse::from)
                .toList();
    }

    public void removeReservations(long reservationId) {
        reservationRepository.deleteById(reservationId);
    }

    private List<Time> extractReservationTimes(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getReservationTime)
                .toList();
    }

    private boolean isTimeBooked(Time time, List<Time> bookedTimes) {
        return bookedTimes.contains(time);
    }
}
