package roomescape.reservation.service;

import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.exception.IllegalReservationDateTimeRequestException;
import roomescape.exception.SaveDuplicateContentException;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.member.dto.MemberProfileInfo;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.dto.AdminReservationRequest;
import roomescape.reservation.dto.ReservationConditionSearchRequest;
import roomescape.reservation.dto.ReservationRequest;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.dto.ReservationTimeAvailabilityResponse;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;
import roomescape.time.dao.TimeDao;
import roomescape.time.domain.Time;

@Service
public class ReservationService {

    private final ReservationDao reservationDao;
    private final TimeDao timeDao;
    private final ThemeDao themeDao;
    private final MemberDao memberDao;

    public ReservationService(ReservationDao reservationDao, TimeDao timeDao, ThemeDao themeDao, MemberDao memberDao) {
        this.reservationDao = reservationDao;
        this.timeDao = timeDao;
        this.themeDao = themeDao;
        this.memberDao = memberDao;
    }

    public ReservationResponse addReservation(ReservationRequest reservationRequest,
            MemberProfileInfo memberProfileInfo) {
        Time time = timeDao.findById(reservationRequest.timeId());
        Theme theme = themeDao.findById(reservationRequest.themeId());
        validateReservationRequest(reservationRequest, time);
        Reservation reservation = reservationRequest.toReservation(time, theme);
        Reservation savedReservation = reservationDao.save(reservation);
        reservationDao.saveMemberReservation(savedReservation.getId(), memberProfileInfo.id());
        return ReservationResponse.fromReservation(savedReservation);
    }

    public ReservationResponse addReservation(AdminReservationRequest reservationRequest) {
        Time time = timeDao.findById(reservationRequest.timeId());
        Theme theme = themeDao.findById(reservationRequest.themeId());
        Member member = memberDao.findById(reservationRequest.memberId());
        Reservation reservation = new Reservation(member.getName(), reservationRequest.date(), time, theme);
        Reservation savedReservation = reservationDao.save(reservation);
        reservationDao.saveMemberReservation(savedReservation.getId(), member.getId());
        return ReservationResponse.fromReservation(savedReservation);
    }

    private void validateReservationRequest(ReservationRequest reservationRequest, Time time) {
        if (reservationRequest.date()
                .isBefore(LocalDate.now())) {
            throw new IllegalReservationDateTimeRequestException("지난 날짜의 예약을 시도하였습니다.");
        }
        validateDuplicateReservation(reservationRequest, time);
    }

    private void validateDuplicateReservation(ReservationRequest reservationRequest, Time time) {
        List<Time> bookedTimes = getBookedTimesOfThemeAtDate(reservationRequest.themeId(), reservationRequest.date());
        if (isTimeBooked(time, bookedTimes)) {
            throw new SaveDuplicateContentException("해당 시간에 예약이 존재합니다.");
        }
    }

    public List<ReservationResponse> findReservations() {
        List<Reservation> reservations = reservationDao.findAllOrderByDateAndTime();

        return reservations.stream()
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    public List<ReservationTimeAvailabilityResponse> findTimeAvailability(long themeId, LocalDate date) {
        List<Time> allTimes = timeDao.findAllReservationTimesInOrder();
        List<Time> bookedTimes = getBookedTimesOfThemeAtDate(themeId, date);

        return allTimes.stream()
                .map(time -> ReservationTimeAvailabilityResponse.fromTime(time, isTimeBooked(time, bookedTimes)))
                .toList();
    }

    public List<ReservationResponse> findReservationsByConditions(ReservationConditionSearchRequest request) {
        List<Long> reservationIds = reservationDao.findReservationIdsByMemberId(request.memberId());
        List<Reservation> reservations = reservationIds.stream()
                .map(reservationDao::findByIdOrderByDate)
                .toList();
        return reservations.stream()
                .filter(reservation -> reservation.isReservedAtPeriod(request.dateFrom(), request.dateTo()))
                .map(ReservationResponse::fromReservation)
                .toList();
    }

    private List<Time> getBookedTimesOfThemeAtDate(long themeId, LocalDate date) {
        List<Reservation> reservationsOfThemeInDate = reservationDao.findAllByThemeIdAndDate(themeId, date);
        return extractReservationTimes(reservationsOfThemeInDate);
    }

    private List<Time> extractReservationTimes(List<Reservation> reservations) {
        return reservations.stream()
                .map(Reservation::getReservationTime)
                .toList();
    }

    private boolean isTimeBooked(Time time, List<Time> bookedTimes) {
        return bookedTimes.contains(time);
    }

    public void removeReservations(long reservationId) {
        reservationDao.deleteById(reservationId);
    }

}
