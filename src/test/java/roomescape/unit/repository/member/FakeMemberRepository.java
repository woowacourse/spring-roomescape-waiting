package roomescape.unit.repository.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.member.Reserver;
import roomescape.repository.member.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    private final AtomicLong index = new AtomicLong(1L);
    private final List<Reserver> reservers = new ArrayList<>();

    @Override
    public Reserver save(Reserver reserver) {
        long id = index.getAndIncrement();
        Reserver createdReserver = Reserver.of(id, reserver);
        reservers.add(createdReserver);
        return createdReserver;
    }

    @Override
    public Optional<Reserver> findById(long id) {
        return reservers.stream()
                .filter((member) -> member.getId().equals(id))
                .findAny();
    }

    @Override
    public Optional<Reserver> findByUsername(String username) {
        return reservers.stream()
                .filter(((member) -> member.isSameUsername(username)))
                .findAny();
    }

    @Override
    public boolean existsByUsername(String username) {
        return reservers.stream()
                .anyMatch(((member) -> member.isSameUsername(username)));
    }

    @Override
    public List<Reserver> findAll() {
        return Collections.unmodifiableList(reservers);
    }
}
