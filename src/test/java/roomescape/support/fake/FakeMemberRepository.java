package roomescape.support.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    private final List<Member> members = new ArrayList<>();
    private Long index = 1L;

    @Override
    public Member save(final Member member) {
        Member newMember = new Member(index++, member.memberName().name(), member.email(), member.password(),
                member.role());
        members.add(newMember);
        return newMember;
    }

    @Override
    public List<Member> findAll() {
        return members;
    }

    @Override
    public Optional<Member> findById(final long id) {
        return members.stream()
                .filter(member -> member.id() == id)
                .findAny();
    }

    @Override
    public Optional<Member> findByEmailAndPassword(final String email, final String password) {
        return members.stream()
                .filter(member -> member.email().equals(email))
                .filter(member -> member.password().equals(password))
                .findAny();
    }
}
