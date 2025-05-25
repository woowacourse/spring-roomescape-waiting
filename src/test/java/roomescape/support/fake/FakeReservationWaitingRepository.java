package roomescape.support.fake;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.reservation.domain.waiting.ReservationWaiting;
import roomescape.reservation.domain.waiting.ReservationWaitingRepository;
import roomescape.reservation.domain.waiting.ReservationWaitingWithRank;

public class FakeReservationWaitingRepository implements ReservationWaitingRepository {

    private final List<ReservationWaiting> reservationWaitings = new ArrayList<>();
    private Long index = 1L;

    @Override
    public boolean existsByReservation(final LocalDate date, final long timeId, final long themeId) {
        return reservationWaitings.stream()
                .anyMatch(reservationWaiting -> reservationWaiting.date() == date &&
                        reservationWaiting.time().id() == timeId &&
                        reservationWaiting.theme().id() == themeId);
    }

    @Override
    public     boolean existsByReservationAndMemberId(LocalDate date, long timeId, long themeId, long memberId) {
        return reservationWaitings.stream()
                .anyMatch(reservationWaiting -> reservationWaiting.date() == date &&
                        reservationWaiting.time().id() == timeId &&
                        reservationWaiting.theme().id() == themeId &&
                        reservationWaiting.member().id() == memberId);
    }

    @Override
    public ReservationWaiting save(final ReservationWaiting reservationWaiting) {
        final ReservationWaiting newReservationWaiting = new ReservationWaiting(index++,
                reservationWaiting.date(), reservationWaiting.time(), reservationWaiting.theme(), reservationWaiting.member());
        reservationWaitings.add(newReservationWaiting);

        return newReservationWaiting;
    }

    @Override
    public void deleteById(final long id) {
        final ReservationWaiting reservationWaiting = findById(id);
        reservationWaitings.remove(reservationWaiting);
    }

    @Override
    public List<ReservationWaiting> findAll() {
        return reservationWaitings;
    }

    @Override
    public List<ReservationWaitingWithRank> findAllWithRankByMemberId(final long memberId) {
        final AtomicLong rank = new AtomicLong(1);

        return reservationWaitings.stream()
                .filter(waiting -> waiting.member().id() == memberId)
                .map(waiting -> new ReservationWaitingWithRank(waiting, rank.getAndIncrement()))
                .toList();
    }

    @Override
    public Optional<ReservationWaiting> findTopByReservation(final LocalDate date, final long timeId,
                                                             final long themeId) {
        return reservationWaitings.stream()
                .filter(waiting -> waiting.date() == date && waiting.time().id() == timeId && waiting.theme().id() == themeId)
                .findFirst();
    }

    private ReservationWaiting findById(final long id) {
        return reservationWaitings.stream()
                .filter(reservation -> reservation.id() == id)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("예약 대기가 존재하지 않습니다."));
    }
}
