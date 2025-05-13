package roomescape.member.service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    private final List<Member> members;

    private AtomicLong index = new AtomicLong(0);

    public FakeMemberRepository(List<Member> members) {
        this.members = members;
    }

    @Override
    public Long save(Member member) {
        long currentIndex = index.incrementAndGet();

        members.add(member.assignId(currentIndex));
        return currentIndex;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return members.stream()
                .filter(member -> Objects.equals(member.getId(), id))
                .findAny();
    }

    @Override
    public boolean existByEmail(String email) {
        return members.stream()
                .anyMatch(member -> member.getEmail().equals(email));
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return members.stream()
                .filter(member -> member.getEmail().equals(email) && member.getPassword().equals(password))
                .findAny();
    }

    @Override
    public List<Member> findAll() {
        return Collections.unmodifiableList(members);
    }
}
