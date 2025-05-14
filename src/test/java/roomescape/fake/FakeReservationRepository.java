package roomescape.fake;

import roomescape.domain.Reservation;
import roomescape.persistence.JpaReservationRepository;
import roomescape.persistence.query.CreateReservationQuery;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class FakeReservationRepository implements JpaReservationRepository {

    private Long id = 0L;
    private List<Reservation> reservations = new ArrayList<>();

    @Override
    public List<Reservation> findAll() {
        return List.copyOf(reservations);
    }

    @Override
    public Long create(CreateReservationQuery createReservationQuery) {
        Reservation newReservation = new Reservation(++id, createReservationQuery.member(), createReservationQuery.date(),
                createReservationQuery.time(), createReservationQuery.theme());
        reservations.add(newReservation);
        return id;
    }

    @Override
    public void deleteById(Long reservationId) {
        reservations = reservations.stream()
                .filter(reservation -> !Objects.equals(reservation.getId(), reservationId))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Reservation> findById(Long reservationId) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), reservationId))
                .findFirst();
    }

    @Override
    public boolean existsByTimeId(final Long reservationTimeId) {
        return reservations.stream()
                .anyMatch(reservation -> Objects.equals(reservation.getTime().getId(), reservationTimeId));
    }

    @Override
    public boolean existsByDateAndTimeIdAndThemeId(final LocalDate reservationDate, final Long timeId, final Long themeId) {
        return reservations.stream()
                .anyMatch(reservation ->
                        reservation.getDate().equals(reservationDate) &&
                        Objects.equals(reservation.getTime().getId(), timeId) &&
                        Objects.equals(reservation.getTime().getId(), themeId));
    }

    @Override
    public boolean existsByThemeId(final Long themeId) {
        return reservations.stream()
                .anyMatch(reservation -> Objects.equals(reservation.getTheme().getId(), themeId));
    }

    @Override
    public List<Reservation> findByThemeIdAndReservationDate(final Long themeId, final LocalDate reservationDate) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getTheme().getId(), themeId))
                .filter(reservation -> reservation.getDate().equals(reservationDate))
                .toList();
    }

    @Override
    public List<Reservation> findReservationsInConditions(final Long memberId, final Long themeId, final LocalDate dateFrom, final LocalDate dateTo) {
        return reservations.stream()
                .filter(reservation -> isMatchingCondition(reservation, memberId, themeId, dateFrom, dateTo))
                .collect(Collectors.toList());
    }

    private boolean isMatchingCondition(Reservation reservation, Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo) {
        if (memberId != null && !reservation.getMember().getId().equals(memberId)) {
            return false;
        }

        if (themeId != null && !reservation.getTheme().getId().equals(themeId)) {
            return false;
        }

        if (dateFrom != null && reservation.getDate().isBefore(dateFrom)) {
            return false;
        }

        return dateTo == null || !reservation.getDate().isAfter(dateTo);
    }
}
