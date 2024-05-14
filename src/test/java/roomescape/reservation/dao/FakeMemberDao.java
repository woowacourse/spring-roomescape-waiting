package roomescape.reservation.dao;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberSignUp;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;

public class FakeMemberDao implements MemberRepository {

    private final Map<Long, Member> members = new HashMap<>();

    @Override
    public Optional<Member> findBy(String email) {
        return members.values().stream()
                .filter(member -> Objects.equals(member.getEmail(), email))
                .findFirst();
    }

    @Override
    public List<Member> findAll() {
        return null;
    }

    @Override
    public boolean existsBy(String email, String password) {
        return true;
    }

    @Override
    public boolean existsBy(String email) {
        return members.values().stream()
                .anyMatch(member -> member.getEmail().equals(email));
    }

    @Override
    public Member save(MemberSignUp memberSignUp) {
        Member member = new Member((long) members.size() + 1, memberSignUp.name(), memberSignUp.email(), Role.USER);
        members.put((long) members.size() + 1, member);
        return member;
    }

    @Override
    public Optional<Member> findById(long id) {
        return Optional.of(members.get(id));
    }
}
