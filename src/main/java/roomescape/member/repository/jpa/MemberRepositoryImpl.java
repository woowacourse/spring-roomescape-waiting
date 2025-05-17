package roomescape.member.repository.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    public MemberRepositoryImpl(final MemberJpaRepository memberJpaRepository) {
        this.memberJpaRepository = memberJpaRepository;
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return memberJpaRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public Optional<Member> findById(long memberId) {
        return memberJpaRepository.findById(memberId);
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    @Override
    public List<Member> findAll() {
        return memberJpaRepository.findAll();
    }
}
