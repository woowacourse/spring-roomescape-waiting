package roomescape.repository.fake;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

public class FakeMemberRepository implements MemberRepository {

    private final Map<Long, Member> data;
    private Long autoIncrementId = 1L;

    public FakeMemberRepository(Map<Long, Member> data) {
        this.data = data;
    }

    @Override
    public Member save(Member member) {
        Member memberToSave = Member.generateWithPrimaryKey(member, autoIncrementId);
        data.put(autoIncrementId, memberToSave);
        autoIncrementId++;

        return memberToSave;
    }

    @Override
    public Optional<Member> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        return Optional.ofNullable(data.get(id));
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        if (email == null) {
            return Optional.empty();
        }
        return data.values().stream()
            .filter(member -> member.getEmail().equals(email))
            .findFirst();
    }

    @Override
    public List<Member> findAll() {
        return List.copyOf(data.values());
    }

    @Override
    public Optional<Member> findByName(String name) {
        if (name == null) {
            return Optional.empty();
        }
        return data.values().stream()
            .filter(member -> member.getName().equals(name))
            .findFirst();
    }
}
