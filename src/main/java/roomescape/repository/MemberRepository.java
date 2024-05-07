package roomescape.repository;

import org.springframework.stereotype.Repository;
import roomescape.model.member.Member;
import roomescape.repository.dao.MemberDao;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberRepository {

    private final MemberDao memberDao;

    public MemberRepository(MemberDao memberDao) {
        this.memberDao = memberDao;
    }

    public Optional<Member> findMemberByEmail(String email) {
        return memberDao.findByEmail(email);
    }

    public Optional<Member> findMemberById(long id) {
        return memberDao.findById(id);
    }

    public List<Member> findAllMembers() {
        return memberDao.findAll();
    }

    public Optional<Member> findMemberByEmailAndPassword(String email, String password) {
        return memberDao.findByEmailAndPassword(email, password);
    }
}
