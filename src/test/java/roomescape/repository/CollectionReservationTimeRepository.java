package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;

public class CollectionReservationTimeRepository implements ReservationTimeRepository {
    private final List<ReservationTime> reservationTimes;
    private final AtomicLong atomicLong;
    private final ReservationRepository reservationRepository;

    public CollectionReservationTimeRepository() {
        this(new ArrayList<>());
    }

    public CollectionReservationTimeRepository(List<ReservationTime> reservationTimes) {
        this(reservationTimes, new AtomicLong(0));
    }

    public CollectionReservationTimeRepository(List<ReservationTime> reservationTimes, AtomicLong atomicLong) {
        this(reservationTimes, atomicLong, null);
    }

    public CollectionReservationTimeRepository(
            List<ReservationTime> reservationTimes,
            AtomicLong atomicLong,
            ReservationRepository reservationRepository
    ) {
        this.reservationTimes = reservationTimes;
        this.atomicLong = atomicLong;
        this.reservationRepository = reservationRepository;
    }

    public CollectionReservationTimeRepository(ReservationRepository reservationRepository) {
        this(new ArrayList<>(), new AtomicLong(), reservationRepository);
    }

    @Override
    public ReservationTime save(ReservationTime reservationTime) {
        ReservationTime saved = new ReservationTime(atomicLong.incrementAndGet(), reservationTime.getStartAt());
        reservationTimes.add(saved);
        return saved;
    }

    @Override
    public boolean existsByStartAt(LocalTime startAt) {
        return reservationTimes.stream()
                .anyMatch(reservationTime -> startAt.equals(reservationTime.getStartAt()));
    }

    @Override
    public Optional<ReservationTime> findById(long id) {
        return reservationTimes.stream()
                .filter(reservationTime -> reservationTime.isIdOf(id))
                .findFirst();
    }

    @Override
    public List<ReservationTime> findAll() {
        return List.copyOf(reservationTimes);
    }

    @Override
    public List<ReservationTime> findUsedTimeByDateAndTheme(LocalDate date, Theme theme) {
        if (reservationRepository == null) {
            throw new UnsupportedOperationException("ReservationRepository 를 사용해 생성하지 않아 메서드를 사용할 수 없습니다.");
        }
        return reservationRepository.findAll().stream()
                .filter(reservation -> date.equals(reservation.getDate()))
                .filter(reservation -> theme.equals(reservation.getTheme()))
                .map(Reservation::getReservationTime)
                .toList();
    }

    @Override
    public void delete(long id) {
        reservationTimes.stream()
                .filter(reservationTime -> reservationTime.isIdOf(id))
                .findAny()
                .ifPresent(reservationTimes::remove);
    }
}
