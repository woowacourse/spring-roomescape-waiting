package roomescape.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    private final Map<Long, Member> members = new HashMap<>();

    private long sequence = 0;

    @Override
    public <S extends Member> S save(final S entity) {
        sequence++;
        Member member = new Member(sequence, entity.getName(), entity.getEmail(), entity.getPassword(),
                entity.getRole());
        members.put(sequence, member);
        return (S) member;
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        return members.values().stream()
                .filter(member -> member.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public List<Member> findAll() {
        return List.copyOf(members.values());
    }

    @Override
    public Optional<String> findNameByEmail(final String email) {
        return members.values().stream()
                .filter(member -> member.getEmail().equals(email))
                .map(Member::getName)
                .findFirst();
    }

    @Override
    public boolean existsByEmailAndPassword(String email, String password) {
        return false;
    }

    @Override
    public Optional<Member> findById(Long memberId) {
        return Optional.empty();
    }

    @Override
    public <S extends Member> Iterable<S> saveAll(Iterable<S> entities) {
        return null;
    }

    @Override
    public boolean existsById(Long aLong) {
        return false;
    }

    @Override
    public Iterable<Member> findAllById(Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(Long aLong) {

    }

    @Override
    public void delete(Member entity) {

    }

    @Override
    public void deleteAllById(Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(Iterable<? extends Member> entities) {

    }

    @Override
    public void deleteAll() {

    }
}
