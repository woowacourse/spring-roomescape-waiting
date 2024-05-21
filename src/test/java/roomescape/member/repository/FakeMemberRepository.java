package roomescape.member.repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.test.util.ReflectionTestUtils;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

public class FakeMemberRepository implements MemberRepository {

    private final Map<Long, Member> members = new ConcurrentHashMap<>();
    private final AtomicLong id = new AtomicLong(0);

    public FakeMemberRepository() {
        long increasedId = id.incrementAndGet();
        members.put(increasedId, makeMember(Member.of("kyunellroll@gmail.com", "polla99"), increasedId));
    }

    @Override
    public Optional<Member> findMemberByEmailAndPassword(Email email, Password password) {
        return members.values().stream()
                .filter(member -> email.getEmail().equals(member.getEmail()))
                .filter(member -> password.getPassword().equals(member.getPassword()))
                .findAny();
    }

    @Override
    public Optional<Member> findMemberById(long id) {
        return Optional.ofNullable(members.get(id));
    }

    @Override
    public List<Member> findAll() {
        return members.values().stream()
                .toList();
    }

    private Member makeMember(Member member, long id) {
        ReflectionTestUtils.setField(member, "id", id);
        return member;
    }
}
