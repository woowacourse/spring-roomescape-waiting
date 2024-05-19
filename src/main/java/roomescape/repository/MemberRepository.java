package roomescape.repository;

import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Members;

@Repository
public class MemberRepository {
    private final MemberDao memberDao;

    public MemberRepository(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Member save(Member member) {
        return memberDao.save(member);
    }

    public Members findAll() {
        return new Members(memberDao.findAll());
    }

    public Optional<Member> findById(long id) {
        return memberDao.findById(id);
    }

    public Optional<Member> findByEmail(Email email) {
        return memberDao.findByEmail(email);
    }

}
