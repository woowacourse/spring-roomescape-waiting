package roomescape.fake;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepositoryInterface;

public class FakeMemberRepository implements MemberRepositoryInterface {

    private final Map<Long, Member> members = new HashMap<>();

    private long sequence = 0;

    @Override
    public Member save(final Member member) {
        sequence++;
        members.put(sequence, member);
        return member;
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        return members.values().stream()
                .filter(member -> member.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public List<Member> findAll() {
        return List.copyOf(members.values());
    }

    @Override
    public Optional<String> findNameByEmail(final String email) {
        return members.values().stream()
                .filter(member -> member.getEmail().equals(email))
                .map(Member::getName)
                .findFirst();
    }

    @Override
    public boolean existsByEmailAndPassword(final String email, final String password) {
        return members.values().stream()
                .anyMatch(member -> member.getEmail().equals(email) && member.getPassword().equals(password));
    }

    @Override
    public Optional<Member> findById(final Long memberId) {
        return Optional.ofNullable(members.get(memberId));
    }

    @Override
    public void deleteById(final Long id) {
        members.remove(id);
    }
}
