package roomescape.member.service.fake;

import roomescape.member.dao.MemberDao;
import roomescape.member.exception.DuplicateMemberException;
import roomescape.member.model.Member;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

public class FakeMemberDao implements MemberDao {

    private final List<Member> members = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    @Override
    public Member add(Member member) {
        validateDuplicate(member.getEmail(), member.getName());
        Member saved = new Member(
                index.getAndIncrement(),
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getRole()
        );
        members.add(saved);
        return saved;
    }

    private void validateDuplicate(String email, String name) {
        boolean isDuplicateEmail = members.stream()
                .anyMatch(member -> member.getEmail().equals(email));
        if (isDuplicateEmail) {
            throw new DuplicateMemberException("이미 가입된 이메일이다.");
        }
        boolean isDuplicateName = members.stream()
                .anyMatch(member -> member.getName().equals(name));
        if (isDuplicateName) {
            throw new DuplicateMemberException("이미 존재하는 이름이다.");
        }
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(members);
    }

    @Override
    public Member findById(Long memberId) {
        return members.stream()
                .filter(m -> m.getId().equals(memberId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 멤버id"));
    }

    @Override
    public Member findByEmail(String email) {
        return members.stream()
                .filter(m -> m.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일"));
    }

    @Override
    public Member findByEmailAndPassword(String email, String password) {
        return members.stream()
                .filter(m -> m.getEmail().equals(email) && m.getPassword().equals(password))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 이메일 또는 패스워드"));
    }

    @Override
    public boolean existByEmail(String email) {
        return members.stream()
                .anyMatch(m -> m.getEmail().equals(email));
    }

    @Override
    public boolean existByName(String name) {
        return members.stream()
                .anyMatch(m -> m.getName().equals(name));
    }
}
