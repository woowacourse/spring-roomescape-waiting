package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Email;
import roomescape.member.domain.Member;
import roomescape.member.domain.Password;

@Repository
public class JpaMemberRepository implements MemberRepository {

    private final MemberListCrudRepository memberListCrudRepository;

    public JpaMemberRepository(MemberListCrudRepository memberListCrudRepository) {
        this.memberListCrudRepository = memberListCrudRepository;
    }

    @Override
    public Member save(Member member) {
        return memberListCrudRepository.save(member);
    }

    @Override
    public List<Member> findAll() {
        return memberListCrudRepository.findAll();
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberListCrudRepository.findById(id);
    }

    @Override
    public Optional<Member> findByEmailAndPassword(Email email, Password password) {
        return memberListCrudRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public boolean existsByEmail(Email email) {
        return memberListCrudRepository.existsByEmail(email);
    }
}
