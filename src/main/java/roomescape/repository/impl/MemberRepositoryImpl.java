package roomescape.repository.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.repository.jpa.MemberJpaRepository;

@RequiredArgsConstructor
@Repository
public class MemberRepositoryImpl implements MemberRepository {

    private final MemberJpaRepository memberJpaRepository;

    @Override
    public Member save(final Member member) {
        return memberJpaRepository.save(member);
    }

    @Override
    public Optional<Member> findById(final long id) {
        return memberJpaRepository.findById(id);
    }

    @Override
    public List<Member> findAll() {
        return memberJpaRepository.findAll();
    }

    @Override
    public Optional<Member> findByEmail(final String email) {
        return memberJpaRepository.findByEmail(email);
    }

    @Override
    public boolean existByEmail(final String email) {
        return memberJpaRepository.existsByEmail(email);
    }

    @Override
    public boolean existByName(final String name) {
        return memberJpaRepository.existsByName(name);
    }
}
