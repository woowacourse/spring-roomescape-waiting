package roomescape.fake;

import roomescape.domain.Member;
import roomescape.persistence.MemberRepository;
import roomescape.persistence.query.CreateMemberQuery;

import java.util.*;

public class FakeMemberRepository implements MemberRepository {

    private Long id = 0L;
    List<Member> members = new ArrayList<>();

    @Override
    public Optional<Member> findByEmail(final String email) {
        return members.stream()
                .filter(member -> Objects.equals(member.getEmail(), email))
                .findFirst();
    }

    @Override
    public Optional<Member> findById(final Long id) {
        return members.stream()
                .filter(member -> Objects.equals(member.getId(), id))
                .findFirst();
    }

    @Override
    public Long create(final CreateMemberQuery createMemberQuery) {
        Member newMember = new Member(++id, createMemberQuery.name(), createMemberQuery.role(), createMemberQuery.email(), createMemberQuery.password());
        members.add(newMember);
        return id;
    }

    @Override
    public List<Member> findAll() {
        return Collections.unmodifiableList(members);
    }
}
