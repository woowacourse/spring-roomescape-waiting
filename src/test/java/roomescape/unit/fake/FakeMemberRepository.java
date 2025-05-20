package roomescape.unit.fake;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.member.domain.Member;
import roomescape.member.infrastructure.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    private final List<Member> members;
    private final AtomicLong index;

    public FakeMemberRepository() {
        this.members = new ArrayList<>();
        this.index = new AtomicLong(1);
    }

    @Override
    public Member save(Member member) {
        Member newMember = member.withId(index.getAndIncrement());
        members.add(newMember);
        return newMember;
    }

    @Override
    public List<Member> findAll() {
        return members;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return members.stream().filter(member -> member.getId().equals(id)).findFirst();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return members.stream().filter(member -> member.getEmail().equals(email)).findFirst();
    }

    public List<Member> getMembers() {
        return members;
    }
}
