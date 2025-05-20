package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.repository.ReservationRepository;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations;
    private final AtomicLong reservationId;

    public FakeReservationRepository(List<Reservation> reservations) {
        this.reservations = new ArrayList<>(reservations);
        this.reservationId = new AtomicLong(reservations.size() + 1);
    }

    @Override
    public Reservation save(Reservation reservation) {
        List<Reservation> existingReservations = findByDateTimeTheme(reservation.getDate(),
                reservation.getTime().getStartAt(), reservation.getTheme().getId());
        if (!existingReservations.isEmpty()) {
            throw new DuplicateKeyException("동일한 예약이 존재합니다.");
        }
        long id = reservationId.getAndIncrement();
        Reservation newReservation = new Reservation(id, reservation.getMember(), reservation.getDate(),
                reservation.getTime(), reservation.getTheme(), ReservationStatus.RESERVED);
        reservations.add(newReservation);
        return newReservation;
    }

    private List<Reservation> findByDateTimeTheme(LocalDate date, LocalTime startAt, Long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getTheme().getId().equals(themeId))
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTime().getStartAt().equals(startAt))
                .toList();
    }

    @Override
    public List<Reservation> findAll() {
        return reservations;
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        return reservations.stream()
                .filter(reservation -> reservation.getMember().getId().equals(memberId))
                .toList();
    }

    @Override
    public List<Reservation> findByDateAndThemeId(LocalDate date, Long themeId) {
        return reservations.stream()
                .filter(reservation -> reservation.getDate().equals(date))
                .filter(reservation -> reservation.getTheme().getId().equals(themeId))
                .toList();
    }

    @Override
    public List<Reservation> findByMemberIdAndThemeIdAndDateRange(Long memberId, Long themeId, LocalDate from,
                                                                  LocalDate to) {
        return reservations.stream()
                .filter(r -> themeId == null || r.getTheme().getId().equals(themeId))
                .filter(r -> memberId == null || r.getMember().getId().equals(memberId))
                .filter(r -> from == null || !r.getDate().isBefore(from))
                .filter(r -> to == null || !r.getDate().isAfter(to))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Reservation> findByTimeId(Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getTime().getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Reservation> findByThemeId(Long id) {
        return reservations.stream()
                .filter(reservation -> reservation.getTheme().getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(LocalDate date, Long timeId, Long themeId) {
        return reservations.stream()
                .filter(r -> r.getTheme().getId().equals(themeId))
                .filter(r -> r.getTime().getId().equals(timeId))
                .anyMatch(r -> r.getDate().equals(date));
    }

    @Override
    public void deleteById(Long id) {
        reservations.removeIf(reservation -> reservation.getId().equals(id));
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), id))
                .findFirst();
    }
}
