package roomescape.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    private final List<Member> members = new ArrayList<>();
    private Long index = 1L;

    public Optional<Member> findByEmailAndPassword(final String email, final String password) {
        return members.stream()
                .filter(member -> member.getEmail().equals(email))
                .filter(member -> member.getPassword().equals(password))
                .findAny();
    }

    public Optional<Member> findById(final long id) {
        return members.stream()
                .filter(member -> member.getId() == id)
                .findAny();
    }

    public Member save(final Member member) {
        Member newMember = new Member(index++, member.getName(), member.getEmail(), member.getPassword(),
                member.getRole());
        members.add(newMember);
        return newMember;
    }

    public List<Member> findAll() {
        return new ArrayList<>(members);
    }
}
