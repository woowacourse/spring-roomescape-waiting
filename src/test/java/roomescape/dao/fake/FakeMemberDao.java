package roomescape.dao.fake;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.MemberDao;
import roomescape.domain.Member;
import roomescape.exception.custom.NotFoundException;

public class FakeMemberDao implements MemberDao {

    private final List<Member> members = new ArrayList<>();
    private final AtomicLong index = new AtomicLong(1);

    public List<Member> findAllMembers() {
        return Collections.unmodifiableList(members);
    }

    public Member findMemberByEmail(String email) {
        return members.stream()
            .filter(m -> m.getEmail().equals(email))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("member"));
    }

    public Member findMemberById(Long id) {
        return members.stream()
            .filter(m -> m.getId().equals(id))
            .findFirst()
            .orElseThrow(() -> new NotFoundException("member"));
    }

    public boolean existMemberByEmail(String email) {
        return members.stream()
            .anyMatch(m -> m.getEmail().equals(email));
    }

    public Member addMember(Member member) {
        Member newMember = new Member(
            index.getAndIncrement(),
            member.getName(),
            member.getEmail(),
            member.getPassword());
        members.add(newMember);
        return newMember;
    }
}
