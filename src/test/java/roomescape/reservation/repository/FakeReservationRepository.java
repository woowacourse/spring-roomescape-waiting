package roomescape.reservation.repository;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;
import roomescape.common.exception.NotFoundException;
import roomescape.member.domain.MemberId;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationId;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.theme.repository.ThemeRepository;
import roomescape.time.domain.ReservationTimeId;

public class FakeReservationRepository implements ReservationRepository {

    private final List<Reservation> reservations = new CopyOnWriteArrayList<>();
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public boolean existsByParams(ReservationTimeId timeId) {
        return reservations.stream()
                .anyMatch(reservation -> Objects.equals(reservation.getTime().getId(), timeId));
    }

    @Override
    public boolean existsByParams(ReservationDate date, ReservationTimeId timeId, ThemeId themeId) {
        return reservations.stream()
                .anyMatch(reservation -> Objects.equals(reservation.getDate(), date)
                        && Objects.equals(reservation.getTime().getId(), timeId)
                        && Objects.equals(reservation.getTheme().getId(), themeId));
    }

    @Override
    public Optional<Reservation> findById(ReservationId id) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), id))
                .findFirst();
    }

    @Override
    public List<Reservation> findByParams(MemberId memberId, ThemeId themeId, ReservationDate from,
                                          ReservationDate to) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getMember().getId(), memberId)
                        && Objects.equals(reservation.getTheme().getId(), themeId)
                        && (reservation.getDate().equals(from) || reservation.getDate().isAfter(from.getValue()))
                        && (reservation.getDate().equals(to) || reservation.getDate().isBefore(to.getValue())))
                .toList();

    }

    @Override
    public List<ReservationTimeId> findTimeIdByParams(ReservationDate date, ThemeId themeId) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getDate(), date)
                        && Objects.equals(reservation.getTheme().getId(), themeId))
                .map(reservation -> reservation.getTime().getId())
                .toList();
    }

    @Override
    public List<Reservation> findAllByMemberId(MemberId memberId) {
        return reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getMember().getId(), memberId))
                .toList();
    }

    @Override
    public List<Reservation> findAll() {
        return new CopyOnWriteArrayList<>(reservations);
    }

    @Override
    public Reservation save(Reservation reservation) {
        Reservation saved = Reservation.withId(
                ReservationId.from(index.getAndIncrement()),
                reservation.getMember(),
                reservation.getDate(),
                reservation.getTime(),
                reservation.getTheme());

        reservations.add(saved);
        return saved;
    }

    @Override
    public void deleteById(ReservationId id) {
        Reservation targetReservation = reservations.stream()
                .filter(reservation -> Objects.equals(reservation.getId(), id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("데이터베이스에 해당 id가 존재하지 않습니다."));

        reservations.remove(targetReservation);
    }

    @Override
    public Map<Theme, Integer> findThemesToBookedCountByParamsOrderByBookedCount(ReservationDate startDate,
                                                                                 ReservationDate endDate, int count) {
        return reservations.stream()
                .filter(reservation -> (reservation.getDate().getValue().isEqual(startDate.getValue()) ||
                        reservation.getDate().getValue().isAfter(startDate.getValue()))
                        && (reservation.getDate().getValue().isEqual(endDate.getValue()) ||
                        reservation.getDate().getValue().isBefore(endDate.getValue())))
                .collect(Collectors.groupingBy(
                        Reservation::getTheme,
                        Collectors.summingInt(reservation -> 1)
                ))
                .entrySet().stream()
                .sorted(Map.Entry.<Theme, Integer>comparingByValue().reversed())
                .limit(count)
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (oldValue, newValue) -> oldValue,
                        LinkedHashMap::new
                ));
    }
}
