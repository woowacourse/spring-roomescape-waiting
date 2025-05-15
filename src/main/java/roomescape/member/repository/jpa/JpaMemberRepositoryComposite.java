package roomescape.member.repository.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.repository.MemberRepository;

@Repository
@ConditionalOnProperty(name = "repository.strategy", havingValue = "jpa")
public class JpaMemberRepositoryComposite implements MemberRepository {
    private final JpaMemberRepository jpaMemberRepository;

    public JpaMemberRepositoryComposite(JpaMemberRepository jpaMemberRepository) {
        this.jpaMemberRepository = jpaMemberRepository;
    }

    @Override
    public Member save(Member member) {
        return jpaMemberRepository.save(member);
    }

    @Override
    public Optional<Member> findById(Long id) {
        return jpaMemberRepository.findById(id);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return jpaMemberRepository.findByEmail(email);
    }

    @Override
    public List<Member> findAll() {
        return jpaMemberRepository.findAll();
    }

    @Override
    public Optional<Member> findByName(String name) {
        return jpaMemberRepository.findByName(name);
    }
}
