package roomescape.member.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    private List<Member> data = new ArrayList<>();
    private AtomicLong atomicLong = new AtomicLong();

    public FakeMemberRepository(final Member... members) {
        data.addAll(List.of(members));
        long maxId = data.stream()
                .mapToLong(Member::getId)
                .max()
                .orElse(0L);
        atomicLong.set(maxId);
    }

    @Override
    public boolean existsByEmail(String email) {
        return data.stream()
                .anyMatch(member -> member.getEmail().equals(email));
    }

    @Override
    public Optional<Member> findById(Long id) {
        return data.stream()
                .filter(m -> m.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return data.stream()
                .filter(m -> m.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return data.stream()
                .filter(m -> m.getEmail().equals(email) && m.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public void save(Member member) {
        final Long newId = atomicLong.incrementAndGet();
        data.add(new Member(newId, member.getName(), member.getEmail(), member.getPassword(), member.getRole()));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(data);
    }
}
