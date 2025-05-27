package roomescape.reservation.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.common.exception.NotFoundException;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new CopyOnWriteArrayList<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public boolean existsByTimeId(Long timeId) {
        return reservations.stream()
                .anyMatch(reservation -> Objects.equals(reservation.getTime().getId(), timeId));
    }

    @Override
    public boolean existsByParams(ReservationDate date, Long timeId, Long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> Objects.equals(reservation.getDate(), date)
                        && Objects.equals(reservation.getTime().getId(), timeId)
                        && Objects.equals(reservation.getTheme().getId(), themeId));
    }

    @Override
    public boolean existsByParams(final ReservationDate date, final Long timeId, final Long themeId,
                                  final Long memberId) {
        return reservations.stream()
                .anyMatch(reservation -> Objects.equals(reservation.getDate(), date)
                        && Objects.equals(reservation.getTime().getId(), timeId)
                        && Objects.equals(reservation.getTheme().getId(), themeId)
                        && Objects.equals(reservation.getMember().getId(), memberId));
    }

    @Override
    public List<Reservation> findByParams(Long memberId, Long themeId, ReservationDate from,
                                          ReservationDate to) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getMember().getId(), memberId)
                        && Objects.equals(reservation.getTheme().getId(), themeId)
                        && (reservation.getDate().equals(from) || reservation.getDate().isAfter(from.getValue()))
                        && (reservation.getDate().equals(to) || reservation.getDate().isBefore(to.getValue())))
                .toList();

    }

    @Override
    public List<Reservation> findByParams(ReservationDate date, Long themeId) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getDate(), date)
                        && Objects.equals(reservation.getTheme().getId(), themeId))
                .toList();
    }

    @Override
    public List<Reservation> findAllByMemberId(Long memberId) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getMember().getId(), memberId))
                .toList();
    }

    @Override
    public Reservation save(Reservation reservation) {
        Reservation saved = Reservation.withId(
                index.getAndIncrement(),
                reservation.getMember(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme()
        );

        reservations.add(saved);
        return saved;
    }

    @Override
    public List<Reservation> findAll() {
        return new ArrayList<>(reservations);
    }

    @Override
    public void deleteById(Long id) {
        Reservation reservation = reservations.stream()
                .filter(value -> Objects.equals(value.getId(), id))
                .findFirst()
                .orElseThrow(NotFoundException::new);

        reservations.remove(reservation);
    }

    @Override
    public Optional<Reservation> findById(Long id) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), id))
                .findFirst();
    }
}
