package roomescape.member.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.common.exception.AlreadyExistException;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;

public class FakeMemberRepository implements MemberRepository {

    private final List<Member> members = Collections.synchronizedList(new ArrayList<>());
    private final AtomicLong index = new AtomicLong(1L);

    @Override
    public boolean existsByEmail(MemberEmail email) {
        return members.stream()
                .anyMatch(member -> member.getEmail().equals(email));
    }

    @Override
    public Member save(Member member) {
        if (existsByEmail(member.getEmail())) {
            throw new AlreadyExistException("이미 존재하는 이메일입니다.");
        }

        Member saved = Member.withId(
                index.getAndIncrement(),
                member.getName(),
                member.getEmail(),
                member.getRole());
        members.add(saved);

        return saved;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return members.stream()
                .filter(member -> Objects.equals(member.getId(), id))
                .findFirst();
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(members);
    }
}
