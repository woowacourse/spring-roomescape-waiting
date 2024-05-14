package roomescape.domain.reservation.service;

import static roomescape.domain.member.domain.Role.MEMBER;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.theme.domain.Theme;

public class FakeReservationRepository implements ReservationRepository {

    Map<Long, Reservation> reservations;
    AtomicLong reservationAtomicLong = new AtomicLong(0);
    AtomicLong reservationTimeAtomicLong = new AtomicLong(0);
    AtomicLong themeAtomicLong = new AtomicLong(0);
    AtomicLong memberAtomicLong = new AtomicLong(0);

    public FakeReservationRepository() {
        this.reservations = new HashMap<>();
    }

    FakeReservationRepository(List<Reservation> reservations) {
        this.reservations = new HashMap<>();
        for (int i = 0; i < reservations.size(); i++) {
            this.reservations.put(reservations.get(i).getId(), reservations.get(i));
        }
    }

    @Override
    public List<Reservation> findAll() {
        return reservations.values().stream().toList();
    }

    @Override
    public List<Reservation> findAllBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        return reservations.values()
                .stream()
                .filter(reservation ->
                        reservation.getThemeId().equals(themeId) &&
                                reservation.getMemberId().equals(memberId) &&
                                (reservation.getDate().isAfter(dateFrom) || reservation.getDate().isEqual(dateFrom)) &&
                                (reservation.getDate().isBefore(dateTo) || reservation.getDate().isEqual(dateTo)))
                .toList();
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        if (reservations.containsKey(id)) {
            return Optional.of(reservations.get(id));
        }
        return Optional.empty();
    }

    @Override
    public Reservation insert(Reservation reservation) {
        Long id = reservationAtomicLong.incrementAndGet();

        ReservationTime reservationTime = reservation.getTime();
        ReservationTime addReservationTime = new ReservationTime(reservationTimeAtomicLong.incrementAndGet(),
                reservationTime.getStartAt());

        Theme theme = reservation.getTheme();
        Theme addTheme = new Theme(themeAtomicLong.incrementAndGet(), theme.getName(), theme.getDescription(),
                theme.getDescription());

        Member member = reservation.getMember();
        Member addMember = new Member(memberAtomicLong.incrementAndGet(), member.getName(), member.getEmail(),
                member.getPassword(), MEMBER);

        Reservation addReservation = new Reservation(id, reservation.getDate(), addReservationTime, addTheme,
                addMember);

        reservations.put(id, addReservation);
        return reservation;
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return reservations.values().stream()
                .anyMatch(reservation -> reservation.getTimeId().equals(timeId) && reservation.getDate().equals(date)
                        && reservation.getThemeId().equals(themeId));
    }

    @Override
    public void deleteById(Long id) {
        reservations.remove(id);
    }

    @Override
    public List<ReservationTime> findTimesByDateAndTheme(LocalDate date, Long themeId) {
        return reservations.values()
                .stream()
                .filter(reservation ->
                        reservation.getDate().isEqual(date) && reservation.getThemeId().equals(themeId))
                .map(reservation -> {
                    return new ReservationTime(reservation.getTimeId(), reservation.getTime().getStartAt());
                })
                .toList();
    }

    @Override
    public List<Theme> findThemeOrderByReservationCount() {
        return null;
    }
}
