package roomescape.domain.member.service;

import static roomescape.domain.member.domain.Role.MEMBER;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.member.domain.Member;
import roomescape.domain.member.repository.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    Map<Long, Member> members = new HashMap<>();
    AtomicLong atomicLong = new AtomicLong(0);

    public Member insert(Member member) {
        Long id = atomicLong.incrementAndGet();

        Member addMember = new Member(id, member.getName(), member.getEmail(), member.getPassword(), MEMBER);
        members.put(id, addMember);
        return addMember;
    }

    @Override
    public Optional<Member> findById(Long id) {
        if (members.containsKey(id)) {
            return Optional.of(members.get(id));
        }
        return Optional.empty();
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return members.values()
                .stream()
                .filter(member -> member.getEmail().equals(email) && member.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public List<Member> findAll() {
        return members.values().stream().toList();
    }
}
