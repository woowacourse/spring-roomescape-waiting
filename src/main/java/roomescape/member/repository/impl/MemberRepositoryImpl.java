package roomescape.member.repository.impl;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.member.domain.MemberEmail;
import roomescape.member.repository.JpaMemberRepository;
import roomescape.member.repository.MemberRepository;

@Repository
@RequiredArgsConstructor
// TODO : 구현체 이름 고민해보기
public class MemberRepositoryImpl implements MemberRepository {

    private final JpaMemberRepository jpaMemberRepository;

    @Override
    public boolean existsByEmail(MemberEmail email) {
        return jpaMemberRepository.existsByEmail(email);
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
    public List<Member> findAll() {
        return jpaMemberRepository.findAll();
    }
}
