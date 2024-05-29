package roomescape.reservation.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Date;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.Time;

public class FakeReservationRepository implements ReservationRepository {

    private static final Time TIME_MOCK_DATA = Time.from(LocalTime.of(9, 0));
    private static final Theme THEME_MOCK_DATA = Theme.of("pollaBang", "폴라 방탈출", "thumbnail");
    private static final Member MEMBER_MOCK_DATA = Member.of("kyunellroll@gmail.com", "polla99");

    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();
    private final AtomicLong id = new AtomicLong(0);

    public FakeReservationRepository() {
        reservations.put(0L, makeReservation(
                Reservation.of(LocalDate.now().plusDays(1), TIME_MOCK_DATA, THEME_MOCK_DATA, MEMBER_MOCK_DATA,
                        ReservationStatus.RESERVED), 0L));

        Long reservationId = id.incrementAndGet();
        reservations.put(reservationId, makeReservation(
                Reservation.of(LocalDate.now().plusDays(1), TIME_MOCK_DATA, THEME_MOCK_DATA, MEMBER_MOCK_DATA,
                        ReservationStatus.WAITING), reservationId));
    }

    @Override
    public Reservation save(Reservation reservation) {
        Long reservationId = id.incrementAndGet();
        reservations.put(reservationId, makeReservation(reservation, reservationId));
        return reservation;
    }

    @Override
    public List<Reservation> findAllByOrderByDateAscTimeAsc() {
        return reservations.values().stream()
                .sorted(Comparator.comparing(Reservation::getDate))
                .sorted(Comparator.comparing(reservation -> reservation.getReservationTime().getStartAt()))
                .toList();
    }

    @Override
    public List<Reservation> findAllByThemeIdAndDate(Long themeId, Date date) {
        return reservations.values().stream()
                .filter(reservation -> isSameThemeId(themeId, reservation))
                .filter(reservation -> isSameDate(date, reservation))
                .toList();
    }

    @Override
    public List<Reservation> findAllByMemberIdAndThemeIdAndDateBetween(Long memberId, Long themeId, Date fromDate,
                                                                       Date toDate) {
        return reservations.values().stream()
                .filter(reservation -> isSameThemeId(themeId, reservation))
                .filter(reservation -> isBetweenDate(fromDate, toDate, reservation))
                .filter(reservation -> isSameMember(memberId, reservation))
                .toList();
    }

    @Override
    public List<Reservation> findByTimeId(Long timeId) {
        return reservations.values().stream()
                .filter(reservation -> isSameTimeId(timeId, reservation))
                .toList();
    }

    @Override
    public Optional<Reservation> findByDateAndMemberIdAndThemeIdAndTimeId(Date date, Long memberId, Long themeId,
                                                                          Long timeId) {
        return reservations.values().stream()
                .filter(reservation -> isSameThemeId(themeId, reservation))
                .filter(reservation -> isSameDate(date, reservation))
                .filter(reservation -> isSameTimeId(timeId, reservation))
                .filter(reservation -> isSameMember(memberId, reservation))
                .findAny();
    }

    @Override
    public Optional<Reservation> findByDateAndMemberIdAndThemeIdAndTimeIdAndReservationStatus(Date date, Long memberId,
                                                                                              Long themeId, Long timeId,
                                                                                              ReservationStatus reservationStatus) {
        return reservations.values().stream()
                .filter(reservation -> isSameThemeId(themeId, reservation))
                .filter(reservation -> isSameDate(date, reservation))
                .filter(reservation -> isSameTimeId(timeId, reservation))
                .filter(reservation -> isSameMember(memberId, reservation))
                .filter(reservation -> isSameStatus(reservationStatus, reservation))
                .findAny();
    }

    @Override
    public List<Reservation> findByThemeId(Long themeId) {
        return reservations.values().stream()
                .filter(reservation -> isSameThemeId(themeId, reservation))
                .toList();
    }

    @Override
    public List<Reservation> findByReservationStatus(ReservationStatus reservationStatus) {
        return reservations.values().stream()
                .filter(reservation -> isSameStatus(reservationStatus, reservation))
                .toList();
    }

    @Override
    public List<Theme> findAllByDateOrderByThemeIdCountLimit(LocalDate startDate, LocalDate endDate, int limitCount) {
        return reservations.values().stream()
                .filter(reservation -> isBetweenDate(Date.dateFrom(startDate), Date.dateFrom(endDate), reservation))
                .sorted(Comparator.comparing(reservation -> reservation.getTheme().getId()))
                .map(Reservation::getTheme)
                .limit(limitCount)
                .toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return Optional.ofNullable(reservations.get(id));
    }

    @Override
    public Optional<Reservation> findFirstByDateAndThemeAndTimeAndReservationStatus(Date date, Theme theme, Time time,
                                                                                    ReservationStatus reservationStatus) {
        return reservations.values().stream()
                .filter(reservation -> isSameDate(date, reservation))
                .filter(reservation -> isSameTimeId(time.getId(), reservation))
                .filter(reservation -> isSameThemeId(theme.getId(), reservation))
                .filter(reservation -> isSameStatus(reservationStatus, reservation))
                .findFirst();
    }

    @Override
    public int countByThemeAndDateAndTimeAndIdLessThan(Theme theme, Date date, Time time, Long waitingId) {
        return (int) reservations.values().stream()
                .filter(reservation -> isSameThemeId(theme.getId(), reservation))
                .filter(reservation -> isSameTimeId(time.getId(), reservation))
                .filter(reservation -> isSameDate(date, reservation))
                .filter(reservation -> reservation.getId() < waitingId)
                .count();
    }

    @Override
    public void deleteById(Long reservationId) {
        reservations.remove((reservationId));
    }

    @Override
    public List<Reservation> findAllByMemberIdAndReservationStatus(Long id, ReservationStatus reservationStatus) {
        return reservations.values().stream()
                .filter(reservation -> isSameMember(id, reservation))
                .filter(reservation -> isSameStatus(reservationStatus, reservation))
                .toList();
    }

    @Override
    public int countByThemeIdAndDateAndTimeIdAndReservationStatus(Long themeId, Date date, Long timeId,
                                                                  ReservationStatus status) {
        return (int) reservations.values().stream()
                .filter(reservation -> isSameThemeId(themeId, reservation))
                .filter(reservation -> isSameStatus(status, reservation))
                .filter(reservation -> isSameDate(date, reservation))
                .filter(reservation -> isSameTimeId(timeId, reservation))
                .count();
    }

    private boolean isSameMember(Long memberId, Reservation reservation) {
        return reservation.getMember().getId() == memberId;
    }

    private boolean isSameThemeId(Long themeId, Reservation reservation) {
        return reservation.getTheme().getId() == themeId;
    }

    private boolean isSameDate(Date date, Reservation reservation) {
        return reservation.getDate().equals(date.getDate());
    }

    private boolean isSameTimeId(Long timeId, Reservation reservation) {
        return reservation.getReservationTime().getId() == timeId;
    }

    private boolean isBetweenDate(Date fromDate, Date toDate, Reservation reservation) {
        return reservation.getDate().isAfter(fromDate.getDate()) && reservation.getDate()
                .isBefore(toDate.getDate());
    }

    private boolean isSameStatus(ReservationStatus reservationStatus, Reservation reservation) {
        return reservationStatus.getStatus().equals(reservation.getReservationStatus());
    }

    private Reservation makeReservation(Reservation reservation, Long id) {
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }
}
