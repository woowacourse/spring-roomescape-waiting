package roomescape.member;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.dao.EmptyResultDataAccessException;

public class FakeMemberRepository implements MemberRepository {

    private final List<Member> members = new ArrayList<>();
    private final List<String> invokeSaveMemberEmail = new ArrayList<>();
    private Long nextId = 1L;

    @Override
    public void saveMember(final Member member) {
        final Member saveMember = new Member(nextId++, member.getEmail(), member.getPassword(), member.getName(), member.getRole());
        members.add(saveMember);
        invokeSaveMemberEmail.add(member.getEmail());
    }

    @Override
    public Member findByEmail(final String email) {
        return members.stream()
                .filter(member -> Objects.equals(member.getEmail(), email))
                .findAny()
                .orElseThrow(() -> new EmptyResultDataAccessException(1));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(members);
    }

    @Override
    public Boolean existsById(final Long id) {
        return members.stream()
                .anyMatch(member -> Objects.equals(member.getId(), id));
    }

    @Override
    public Boolean existsByEmail(final String email) {
        return members.stream()
                .anyMatch(member -> Objects.equals(member.getEmail(), email));
    }

    public void clear() {
        members.clear();
        invokeSaveMemberEmail.clear();
        nextId = 1L;
    }

    public boolean isInvokeSaveMemberEmail(final String invokeEmail) {
        return invokeSaveMemberEmail.stream()
                .anyMatch(email -> Objects.equals(email, invokeEmail));
    }
}
