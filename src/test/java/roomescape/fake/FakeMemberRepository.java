package roomescape.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    List<Member> members = new ArrayList<>();
    Long index = 1L;

    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return members.stream()
                .filter(member -> member.getEmail().equals(email))
                .filter(member -> member.getPassword().equals(password))
                .findAny();
    }

    public Optional<Member> findById(long memberId) {
        return members.stream()
                .filter(member -> member.getId() == memberId)
                .findAny();
    }

    public Member save(Member member) {
        Member newMember = new Member(index++, member.getName(), member.getEmail(), member.getPassword(),
                member.getRole());
        members.add(newMember);
        return newMember;
    }

    public List<Member> findAll() {
        return new ArrayList<>(members);
    }
}
