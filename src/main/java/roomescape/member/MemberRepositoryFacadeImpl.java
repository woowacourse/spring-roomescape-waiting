package roomescape.member;

import java.util.List;
import java.util.Optional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@AllArgsConstructor
public class MemberRepositoryFacadeImpl implements MemberRepositoryFacade{

    private final MemberRepository memberRepository;

    public void save(Member member) {
        memberRepository.save(member);
    }

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
    public boolean existsByEmail(final String email) {
        return memberRepository.existsByEmail(email);
    }
}
