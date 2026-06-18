package roomescape.member.adapter.out.persistence;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.member.application.port.out.MemberRepository;
import roomescape.member.domain.Member;

@Repository
@RequiredArgsConstructor
public class JpaMemberRepository implements MemberRepository {
    private final SpringDataMemberRepository repository;

    @Override
    public Optional<Member> findByName(String name) {
        return repository.findByName(name);
    }

    @Override
    public Optional<Member> findById(long id) {
        return repository.findById(id);
    }
}
