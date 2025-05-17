package roomescape.member.infrastructure;

import java.util.Collection;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;

@Repository
public class MemberRepositoryAdaptor implements MemberRepository {
    private final MemberJpaRepository memberJpaRepository;
    private final MemberJdbcDao memberJdbcDao;

    public MemberRepositoryAdaptor(MemberJpaRepository memberJpaRepository, MemberJdbcDao memberJdbcDao) {
        this.memberJpaRepository = memberJpaRepository;
        this.memberJdbcDao = memberJdbcDao;
    }


    @Override
    public Optional<Member> findByEmail(String email) {
        return memberJpaRepository.findByEmail(email);
    }

    @Override
    public boolean existsByEmail(String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public Collection<Member> findAll() {
        return memberJpaRepository.findAll();
    }

    @Override
    public Member save(Member member) {
        return memberJpaRepository.save(member);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return memberJpaRepository.findById(id);
    }
}
