package roomescape.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.domain.Password;
import roomescape.member.repository.MemberRepository;

public class FakeMemberDao implements MemberRepository {

    private final List<Member> members = new ArrayList<>();
    private long index = 0;

    @Override
    public Member save(final Member member) {
        Member savedMember = new Member(++index, member.getName(), member.getEmail(), member.getPassword(),
                member.getRole());
        members.add(savedMember);
        return savedMember;
    }

    @Override
    public Optional<Member> findById(final Long id) {
        return members.stream()
                .filter(member -> member.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Member> findByEmailAndPassword(final MemberEmail email, final Password password) {
        return members.stream()
                .filter(member -> member.getEmail().equals(email) && member.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public List<Member> findAll() {
        return members;
    }

    @Override
    public boolean existsByEmail(final MemberEmail email) {
        return members.stream()
                .anyMatch(member -> member.getEmail().equals(email));
    }

    @Override
    public boolean existsByName(final MemberName name) {
        return members.stream()
                .anyMatch(member -> member.getName().equals(name));
    }

    @Override
    public <S extends Member> Iterable<S> saveAll(final Iterable<S> entities) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public boolean existsById(final Long aLong) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public Iterable<Member> findAllById(final Iterable<Long> longs) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public long count() {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteById(final Long aLong) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void delete(final Member entity) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAllById(final Iterable<? extends Long> longs) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAll(final Iterable<? extends Member> entities) {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }

    @Override
    public void deleteAll() {
        throw new IllegalStateException("사용하지 않는 메서드입니다.");
    }
}
