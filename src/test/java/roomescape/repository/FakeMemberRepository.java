package roomescape.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.dao.DuplicateKeyException;
import roomescape.domain.LoginMember;
import roomescape.domain.RegistrationDetails;

public class FakeMemberRepository implements MemberRepository {

    private final List<LoginMember> members;
    private final AtomicLong memberId;

    public FakeMemberRepository(final List<LoginMember> members) {
        this.members = new ArrayList<>(members);
        this.memberId = new AtomicLong(members.size() + 1);
    }

    @Override
    public void save(RegistrationDetails registrationDetails) {
        long count = members.stream()
                .filter(member -> member.getEmail().equalsIgnoreCase(registrationDetails.email()))
                .count();
        if (count != 0) {
            throw new DuplicateKeyException("이미 가입한 이메일입니다.");
        }

        long id = memberId.getAndIncrement();
        LoginMember newMember = new LoginMember(id, registrationDetails.name(), registrationDetails.email(),
                registrationDetails.password(), "USER");
        members.add(newMember);
    }

    @Override
    public Optional<LoginMember> findByEmail(String email) {
        return members.stream()
                .filter(member -> member.getEmail().equalsIgnoreCase(email))
                .findFirst();
    }

    @Override
    public Optional<LoginMember> findByEmailAndPassword(String email, String password) {
        return members.stream()
                .filter(member -> member.getEmail().equalsIgnoreCase(email) && member.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public Optional<LoginMember> findById(long id) {
        return members.stream()
                .filter(member -> member.getId() == id)
                .findFirst();
    }

    @Override
    public List<LoginMember> findAll() {
        return members;
    }
}
