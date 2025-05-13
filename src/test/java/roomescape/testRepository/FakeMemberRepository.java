package roomescape.testRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;
import roomescape.domain.repository.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    private final List<Member> members = new ArrayList<>();

    private Long index = 0L;

    @Override
    public List<Member> findAll() {
        return members;
    }

    @Override
    public Long save(Member member) {
        Member memberWithId = Member.assignId(++index, member);
        members.add(memberWithId);
        return index;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return members.stream()
                .filter(member -> member.getId().equals(id))
                .findFirst();
    }

    @Override
    public Optional<Member> findBy(String email, String password) {
        return Optional.empty();
    }
}
