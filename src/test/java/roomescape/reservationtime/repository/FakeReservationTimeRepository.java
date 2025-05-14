//package roomescape.reservationtime.repository;
//
//import java.time.LocalTime;
//import java.util.List;
//import java.util.Map;
//import java.util.Optional;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.atomic.AtomicLong;
//import roomescape.reservationtime.domain.ReservationTime;
//
//public class FakeReservationTimeRepository implements ReservationTimeRepository {
//
//    private final Map<Long, ReservationTime> reservationTimes = new ConcurrentHashMap<>();
//    private final AtomicLong index = new AtomicLong(1);
//
//    @Override
//    public List<ReservationTime> findAll() {
//        return reservationTimes.entrySet().stream()
//                .map(entry -> ReservationTime.of(entry.getKey(), entry.getValue().getStartAt()))
//                .toList();
//    }
//
//    @Override
//    public ReservationTime save(final ReservationTime reservationTime) {
//        Long id = index.getAndIncrement();
//        reservationTimes.put(id, reservationTime);
//        return ReservationTime.of(id, reservationTime.getStartAt());
//    }
//
//    @Override
//    public boolean deleteById(final Long id) {
//        return reservationTimes.remove(id) != null;
//    }
//
//    @Override
//    public Optional<ReservationTime> findById(final Long id) {
//        return Optional.ofNullable(reservationTimes.get(id))
//                .map(reservationTime -> ReservationTime.of(id, reservationTime.getStartAt()));
//    }
//
//    @Override
//    public boolean checkExistsByStartAt(final LocalTime time) {
//        return reservationTimes.values().stream()
//                .map(ReservationTime::getStartAt)
//                .anyMatch(startAt -> startAt.equals(time));
//    }
//}
