package roomescape.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.domain.MemberName;
import roomescape.member.repository.MemberRepository;

public class FakeMemberDao implements MemberRepository {

    private final List<Member> members = new ArrayList<>();
    private int index = 0;

    @Override
    public Member save(final Member member) {
        Member savedMember = member.withId(++index);
        members.add(savedMember);
        return savedMember;
    }

    @Override
    public <S extends Member> Iterable<S> saveAll(final Iterable<S> entities) {
        return null;
    }

    @Override
    public Optional<Member> findById(final Long id) {
        return members.stream()
                .filter(member -> member.getId().equals(id))
                .findFirst();
    }

    @Override
    public boolean existsById(final Long aLong) {
        return false;
    }

    @Override
    public List<Member> findAll() {
        return members;
    }

    @Override
    public Iterable<Member> findAllById(final Iterable<Long> longs) {
        return null;
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public void deleteById(final Long aLong) {

    }

    @Override
    public void delete(final Member entity) {

    }

    @Override
    public void deleteAllById(final Iterable<? extends Long> longs) {

    }

    @Override
    public void deleteAll(final Iterable<? extends Member> entities) {

    }

    @Override
    public void deleteAll() {

    }

    @Override
    public Optional<Member> findByEmailAndPassword(final MemberEmail email, final String password) {
        return members.stream()
                .filter(member -> member.getMemberEmail().equals(email) && member.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public boolean existsByEmail(final MemberEmail email) {
        return members.stream()
                .anyMatch(member -> member.getMemberEmail().equals(email));
    }

    @Override
    public boolean existsByName(final MemberName name) {
        return members.stream()
                .anyMatch(member -> member.getMemberName().equals(name));
    }
}
