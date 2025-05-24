package roomescape.fake;

import roomescape.reservation.waiting.domain.Waiting;
import roomescape.reservation.waiting.domain.WaitingWithRank;
import roomescape.reservation.waiting.repository.WaitingRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class FakeWaitingDao implements WaitingRepository {

    List<Waiting> waitings = new ArrayList<>();
    Long index = 1L;

    @Override
    public Waiting save(final Waiting waiting) {
        Waiting newWaiting = new Waiting(index++, waiting.getMember(), waiting.getDate(),
                waiting.getTime(), waiting.getTheme());
        waitings.add(newWaiting);
        return newWaiting;
    }

    @Override
    public List<Waiting> findAll() {
        return waitings;
    }

    @Override
    public Optional<Waiting> findById(Long id) {
        return waitings.stream()
                .filter(reservation -> reservation.getId().equals(id))
                .findFirst();
    }

    @Override
    public List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId) {
        return List.of();
    }

    @Override
    public <S extends Waiting> Iterable<S> saveAll(Iterable<S> entities) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public boolean existsById(Long aLong) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public Iterable<Waiting> findAllById(Iterable<Long> longs) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public long count() {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteById(Long aLong) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void delete(Waiting entity) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAll(Iterable<? extends Waiting> entities) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAll() {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }
}
