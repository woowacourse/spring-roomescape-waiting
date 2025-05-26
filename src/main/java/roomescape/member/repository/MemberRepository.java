package roomescape.member.repository;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;

@RequiredArgsConstructor
@Repository
public class MemberRepository implements MemberRepositoryInterface {

    private final JpaMemberRepository jpaMemberRepository;

    @Override
    public Optional<Member> findByEmail(final String email) {
        return jpaMemberRepository.findByEmail(email);
    }

    @Override
    public Optional<Member> findById(final Long id) {
        return jpaMemberRepository.findById(id);
    }

    @Override
    public List<Member> findAll() {
        return jpaMemberRepository.findAll();
    }

    @Override
    public Member save(final Member member) {
        return jpaMemberRepository.save(member);
    }

    @Override
    public void deleteById(final Long id) {
        jpaMemberRepository.deleteById(id);
    }

    @Override
    public boolean existsByEmailAndPassword(final String email, final String password) {
        return jpaMemberRepository.existsByEmailAndPassword(email, password);
    }

    @Override
    public Optional<String> findNameByEmail(final String email) {
        return jpaMemberRepository.findNameByEmail(email);
    }
}
