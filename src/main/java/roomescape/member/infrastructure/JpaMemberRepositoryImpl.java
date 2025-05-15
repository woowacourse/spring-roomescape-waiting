package roomescape.member.infrastructure;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberRepository;

@Repository
public class JpaMemberRepositoryImpl implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    public JpaMemberRepositoryImpl(JpaMemberRepository jpaMemberRepository) {
        this.jpaMemberRepository = jpaMemberRepository;
    }

    @Override
    public Member save(Member member) {
        return jpaMemberRepository.save(member);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaMemberRepository.existsByEmail(email);
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return jpaMemberRepository.findByEmailAndPassword(email, password);
    }

    @Override
    public Optional<Member> findById(Long memberId) {
        return jpaMemberRepository.findById(memberId);
    }

    @Override
    public List<Member> findAll() {
        return jpaMemberRepository.findAll();
    }
}
