package roomescape.service.fakedao;

import roomescape.model.member.Member;
import roomescape.repository.dao.MemberDao;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

public class FakeMemberDao implements MemberDao {

    private final AtomicLong index = new AtomicLong(1);
    private final List<Member> members = new ArrayList<>();

    public FakeMemberDao(List<Member> members) {
        members.forEach(this::save);
    }

    @Override
    public List<Member> findAll() {
        return members;
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        Member result = members.stream()
                .filter(member -> member.getEmail().equals(email))
                .findFirst()
                .orElseThrow(() -> null);
        return Optional.ofNullable(result);
    }

    @Override
    public Optional<Member> findById(long id) {
        Member result = members.stream()
                .filter(member -> member.getId() == id)
                .findFirst()
                .orElseThrow(() -> null);
        return Optional.ofNullable(result);
    }

    private long save(Member rawMember) {
        long key = index.getAndIncrement();
        Member member = new Member(key, rawMember.getName(), rawMember.getEmail(), rawMember.getPassword(), rawMember.getRole());
        members.add(member);
        return key;
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return members.stream()
                .filter(member -> member.getEmail().equals(email) && member.getPassword().equals(password))
                .findFirst();
    }
}
