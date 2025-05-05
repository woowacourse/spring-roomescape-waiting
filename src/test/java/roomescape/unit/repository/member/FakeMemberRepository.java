package roomescape.unit.repository.member;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.member.Member;
import roomescape.repository.member.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    AtomicLong index = new AtomicLong(1L);
    List<Member> members = new ArrayList<>();

    @Override
    public long add(Member member) {
        long id = index.getAndIncrement();
        members.add(Member.of(id, member));
        return id;
    }

    @Override
    public boolean existsByEmailAndPassword(Member compareMember) {
        return members.stream()
                .anyMatch((member) -> compareMember.isSameUserInfo(compareMember));
    }

    @Override
    public Optional<Member> findById(long id) {
        return members.stream()
                .filter((member) -> member.getId().equals(id))
                .findAny();
    }
}
