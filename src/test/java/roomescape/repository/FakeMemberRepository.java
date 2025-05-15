package roomescape.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.Member;
import roomescape.domain.Role;

public class FakeMemberRepository implements MemberRepository {

    private final List<Member> members;
    private final AtomicLong memberId;

    public FakeMemberRepository(final List<Member> members) {
        this.members = new ArrayList<>(members);
        this.memberId = new AtomicLong(members.size() + 1);
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return members.stream()
                .filter(member -> member.getEmail().equalsIgnoreCase(email) && member.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public boolean existsByEmail(String email) {
        return members.stream()
                .anyMatch(member -> member.getEmail().equalsIgnoreCase(email));
    }

    @Override
    public Member save(Member member) {
        long count = members.stream()
                .filter(m -> m.getEmail().equalsIgnoreCase(member.getEmail()))
                .count();
        if (count != 0) {
            throw new DuplicateKeyException("이미 가입한 이메일입니다.");
        }

        long id = memberId.getAndIncrement();
        Member newMember = new Member(id, member.getName(), member.getEmail(), member.getPassword(), Role.USER);
        members.add(newMember);
        return newMember;
    }

    @Override
    public List<Member> findAll() {
        return members;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return members.stream()
                .filter(member -> member.getId().equals(id))
                .findFirst();
    }
}
