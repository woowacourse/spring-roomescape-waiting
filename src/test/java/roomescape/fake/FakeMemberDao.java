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
    public List<Member> findAll() {
        return members;
    }

    @Override
    public Optional<Member> findById(final long id) {
        return members.stream()
                .filter(member -> member.getId().equals(id))
                .findFirst();
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
