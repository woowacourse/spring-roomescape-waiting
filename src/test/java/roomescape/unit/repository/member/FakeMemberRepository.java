package roomescape.unit.repository.member;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.member.Member;
import roomescape.repository.member.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    private final AtomicLong index = new AtomicLong(1L);
    private final List<Member> members = new ArrayList<>();

    @Override
    public Member save(Member member) {
        long id = index.getAndIncrement();
        Member createdMember = Member.of(id, member);
        members.add(createdMember);
        return createdMember;
    }

    @Override
    public Optional<Member> findById(long id) {
        return members.stream()
                .filter((member) -> member.getId().equals(id))
                .findAny();
    }

    @Override
    public Optional<Member> findByUsername(String username) {
        return members.stream()
                .filter(((member) -> member.isSameUsername(username)))
                .findAny();
    }

    @Override
    public boolean existsByUsername(String username) {
        return members.stream()
                .anyMatch(((member) -> member.isSameUsername(username)));
    }

    @Override
    public List<Member> findAll() {
        return Collections.unmodifiableList(members);
    }
}
