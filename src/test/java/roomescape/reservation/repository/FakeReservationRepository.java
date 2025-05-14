package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.domain.Reservation;
import roomescape.reservationtime.dto.response.AvailableReservationTimeResponse;

public class FakeReservationRepository implements ReservationRepository {

    private final Map<Long, Reservation> reservations = new ConcurrentHashMap<>();
    private final AtomicLong index = new AtomicLong(1);

    @Override
    public List<Reservation> findFilteredReservations(final Long themeId, final Long memberId, final LocalDate dateFrom,
                                                      final LocalDate dateTo) {
        return makeFilteredReservations(themeId, memberId, dateFrom, dateTo).stream()
                .map(entry -> {
                    Reservation value = entry.getValue();
                    return Reservation.of(entry.getKey(), value.getDate(), value.getMember(),
                            value.getTime(), value.getTheme());
                })
                .toList();
    }

    private List<Entry<Long, Reservation>> makeFilteredReservations(final Long themeId, final Long memberId,
                                                                    final LocalDate dateFrom,
                                                                    final LocalDate dateTo) {
        List<Entry<Long, Reservation>> filteredReservations = new ArrayList<>();
        for (Entry<Long, Reservation> entry : reservations.entrySet()) {
            Reservation reservation = entry.getValue();
            if (themeId != null && !Objects.equals(reservation.getTheme().getId(), themeId)) {
                continue;
            }
            if (memberId != null && !Objects.equals(reservation.getMember().getId(), memberId)) {
                continue;
            }
            if (dateFrom != null && (reservation.getDate().isAfter(dateFrom))) {
                continue;
            }
            if (dateTo != null && (reservation.getDate().isBefore(dateTo))) {
                continue;
            }
            filteredReservations.add(entry);
        }
        return filteredReservations;
    }

    @Override
    public Reservation save(final Reservation reservation) {
        Long id = index.getAndIncrement();
        reservations.put(id, reservation);
        return Reservation.of(id, reservation.getDate(), reservation.getMember(), reservation.getTime(),
                reservation.getTheme());
    }

    @Override
    public boolean deleteById(final Long id) {
        return reservations.remove(id) != null;
    }

    @Override
    public boolean existsByTimeId(final Long id) {
        return reservations.values().stream()
                .anyMatch(reservation -> Objects.equals(reservation.getTime().getId(), id));
    }

    @Override
    public boolean existsByThemeId(final Long id) {
        return reservations.values().stream()
                .anyMatch(reservation -> Objects.equals(reservation.getTheme().getId(), id));
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate date, final Long timeId, final Long themeId) {
        return reservations.values().stream()
                .anyMatch(reservation -> Objects.equals(reservation.getDate(), date) && Objects.equals(
                        reservation.getTime().getId(), timeId) && Objects.equals(reservation.getTheme().getId(),
                        themeId));
    }

    @Override
    public List<AvailableReservationTimeResponse> findAvailableTimesByDateAndThemeId(final LocalDate date,
                                                                                     final Long themeId) {
        return reservations.values()
                .stream()
                .filter(reservation -> reservation.getDate().equals(date) && reservation.getTheme().getId()
                        .equals(themeId))
                .map(reservation -> new AvailableReservationTimeResponse(reservation.getTime().getId(),
                        reservation.getTime()
                                .getStartAt(), true))
                .toList();
    }
}

