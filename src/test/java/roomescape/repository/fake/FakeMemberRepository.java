package roomescape.repository.fake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.aspectj.apache.bcel.Repository;
import roomescape.repository.MemberRepository;
import roomescape.entity.Member;
import roomescape.exception.custom.NotFoundException;

public class FakeMemberRepository implements MemberRepository {

    private final List<Member> members = new ArrayList<>();

    public List<Member> findAll() {
        return Collections.unmodifiableList(members);
    }

    public Member findByEmail(String email) {
        return members.stream()
            .filter(m -> m.getEmail().equals(email))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("member"));
    }

    public Optional<Member> findById(Long id) {
        return members.stream()
            .filter(m -> m.getId().equals(id))
            .findFirst();
    }

    public boolean existsByEmail(String email) {
        return members.stream()
            .anyMatch(m -> m.getEmail().equals(email));
    }

    public Member save(Member member) {
        Member newMember = new Member(
            member.getName(),
            member.getEmail(),
            member.getPassword());
        members.add(newMember);
        return newMember;
    }
}
