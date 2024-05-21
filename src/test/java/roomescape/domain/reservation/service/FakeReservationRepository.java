package roomescape.domain.reservation.service;

import static roomescape.domain.member.domain.Role.MEMBER;
import static roomescape.domain.reservation.domain.reservation.ReservationStatus.RESERVED;
import static roomescape.fixture.TimestampFixture.TIMESTAMP_BEFORE_ONE_YEAR;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.member.domain.Member;
import roomescape.domain.reservation.domain.reservation.Reservation;
import roomescape.domain.reservation.domain.reservationTime.ReservationTime;
import roomescape.domain.reservation.dto.ReservationWithOrderDto;
import roomescape.domain.reservation.repository.reservation.ReservationRepository;
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

    @Override
    public List<Reservation> findAll() {
        return reservations.values().stream().toList();
    }

    @Override
    public List<Reservation> findAllBy(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo) {
        return reservations.values()
                .stream()
                .filter(reservation ->
                        reservation.getTheme().getId().equals(themeId) &&
                                reservation.getMember().getId().equals(memberId) &&
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
    public Reservation save(Reservation reservation) {
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
                addMember, RESERVED, TIMESTAMP_BEFORE_ONE_YEAR);

        reservations.put(id, addReservation);
        return reservation;
    }

    @Override
    public boolean existByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return reservations.values().stream()
                .anyMatch(reservation -> reservation.getTime().getId().equals(timeId) && reservation.getDate()
                        .equals(date)
                        && reservation.getTheme().getId().equals(themeId));
    }

    @Override
    public boolean existByMemberIdAndDateAndTimeIdAndThemeId(Long memberId, LocalDate date, Long timeId, Long themeId) {
        return reservations.values().stream()
                .anyMatch(reservation ->
                        reservation.getMember().getId().equals(memberId) &&
                                reservation.getTime().getId().equals(timeId) &&
                                reservation.getDate().equals(date) &&
                                reservation.getTheme().getId().equals(themeId));
    }

    @Override
    public void deleteById(Long id) {
        reservations.remove(id);
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return reservations.values()
                .stream()
                .filter(reservation ->
                        reservation.getDate().isEqual(date) && reservation.getTheme().getId().equals(themeId))
                .toList();
    }

    @Override
    public List<ReservationWithOrderDto> findByMemberId(Long memberId) {
        List<Reservation> memberReservations = reservations.values()
                .stream()
                .filter(reservation -> reservation.getMember().getId().equals(memberId))
                .toList();
        List<ReservationWithOrderDto> result = new ArrayList<>();

        for (Reservation reservation : memberReservations) {
            long rank = reservations.values().stream()
                    .filter(r -> r.getDate().equals(reservation.getDate())
                            && r.getTheme().getId().equals(reservation.getTheme().getId())
                            && r.getTime().getId().equals(reservation.getTime().getId())
                            && r.getReservationTimestamp().isBefore(reservation.getReservationTimestamp()))
                    .count();

            result.add(new ReservationWithOrderDto(reservation, rank));
        }
        return result;
    }
}
