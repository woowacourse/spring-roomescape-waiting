package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;

@RequiredArgsConstructor
@Repository
public class JpaMemberRepository implements MemberRepositoryInterface {

    private final MemberRepository memberRepository;

    @Override
    public Optional<Member> findByEmail(final String email) {
        return memberRepository.findByEmail(email);
    }

    @Override
    public Optional<Member> findById(final Long id) {
        return memberRepository.findById(id);
    }

    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    @Override
    public Member save(final Member member) {
        return memberRepository.save(member);
    }

    @Override
    public void deleteById(final Long id) {
        memberRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmailAndPassword(final String email, final String password) {
        return memberRepository.existsByEmailAndPassword(email, password);
    }

    @Override
    public Optional<String> findNameByEmail(final String email) {
        return memberRepository.findNameByEmail(email);
    }
}
