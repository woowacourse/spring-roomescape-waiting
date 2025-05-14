package roomescape.member.infrastructure.db;

import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import roomescape.member.model.Member;
import roomescape.member.model.MemberRepository;

@Repository
@RequiredArgsConstructor
public class MemberDbRepository implements MemberRepository {

    private final MemberDao memberDao;

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return memberDao.selectByEmailAndPassword(email, password);
    }

    @Override
    public List<Member> getAll() {
        return memberDao.getAll();
    }

    @Override
    public boolean existsById(Long memberId) {
        return memberDao.existsById(memberId);
    }
}
