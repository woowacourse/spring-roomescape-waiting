package roomescape.unit.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.domain.TimeSlot;
import roomescape.reservation.dto.request.ReservationCondition;
import roomescape.reservation.infrastructure.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    public FakeReservationRepository(Reservation... reservations) {
        Arrays.stream(reservations).forEach(reservation -> this.reservations.add(reservation));
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations);
    }

    @Override
    public Reservation save(Reservation reservation) {
        Reservation reservationWithId = Reservation.builder()
                .id(index.getAndIncrement())
                .member(reservation.getMember())
                .date(reservation.getDate())
                .timeSlot(reservation.getTimeSlot())
                .theme(reservation.getTheme()).build();
        reservations.add(reservationWithId);
        return reservationWithId;
    }

    @Override
    public void deleteById(Long id) {
        reservations.removeIf(reservation -> reservation.getId().equals(id));
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<Reservation> findByDateAndTheme(LocalDate date, Theme theme) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTheme().getId().equals(theme.getId()))
                .toList();
    }

    @Override
    public boolean existsByThemeId(Long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTheme().getId().equals(themeId));
    }

    @Override
    public Optional<Reservation> findByDateAndTimeSlotAndTheme(LocalDate date, TimeSlot time,
                                                               Theme theme) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTimeSlot().equals(time))
                .filter(reservation -> reservation.getTheme().equals(theme))
                .findFirst();
    }

    @Override
    public List<Reservation> findByDateBetween(LocalDate dateFrom, LocalDate dateTo) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().plusDays(1).isAfter(dateFrom))
                .filter(reservation -> reservation.getDate().minusDays(1).isBefore(dateTo))
                .toList();
    }

    @Override
    public boolean existsByTimeSlotId(Long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> reservation.getTimeSlot().getId().equals(timeId));
    }

    @Override
    public List<Reservation> findByCondition(ReservationCondition condition) {
        List<Reservation> filteredReservations = new ArrayList<>(reservations);
        if (condition.themeId() != null) {
            filteredReservations = filteredReservations.stream()
                    .filter(reservation -> reservation.getTheme().getId().equals(condition.themeId()))
                    .toList();
        }
        if (condition.memberId() != null) {
            filteredReservations = filteredReservations.stream()
                    .filter(reservation -> reservation.getMember().getId().equals(condition.memberId()))
                    .toList();
        }
        if (condition.dateFrom() != null && condition.dateTo() != null) {
            filteredReservations = filteredReservations.stream()
                    .filter(reservation -> reservation.getDate().plusDays(1).isAfter(condition.dateFrom()))
                    .filter(reservation -> reservation.getDate().minusDays(1).isBefore(condition.dateTo()))
                    .toList();
        }
        return filteredReservations;
    }

    @Override
    public List<Reservation> findByMemberId(Long memberId) {
        return reservations.stream()
                .filter(reservation -> reservation.getMember().getId().equals(memberId))
                .toList();
    }

    @Override
    public boolean existsByDateAndMemberAndThemeAndTimeSlot(LocalDate date, Member member, Theme theme,
                                                            TimeSlot timeSlot) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTimeSlot().equals(timeSlot))
                .filter(reservation -> reservation.getMember().equals(member))
                .anyMatch(reservation -> reservation.getTheme().equals(theme));
    }

    @Override
    public boolean existsByDateAndThemeAndTimeSlot(LocalDate date, Theme theme, TimeSlot timeSlot) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTimeSlot().equals(timeSlot))
                .anyMatch(reservation -> reservation.getTheme().equals(theme));
    }

    @Override
    public void delete(Reservation reservation) {
        reservations.remove(reservation);
    }
}
