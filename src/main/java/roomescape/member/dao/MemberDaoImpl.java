package roomescape.member.dao;

import org.springframework.stereotype.Repository;
import roomescape.member.model.Member;

import java.util.List;
import java.util.Optional;

@Repository
public class MemberDaoImpl implements MemberDao {

    private final JpaMemberDao jpaMemberDao;

    public MemberDaoImpl(JpaMemberDao jpaMemberDao) {
        this.jpaMemberDao = jpaMemberDao;
    }

    @Override
    public Member save(Member member) {
        return jpaMemberDao.save(member);
    }

    @Override
    public List<Member> findAll() {
        return jpaMemberDao.findAll();
    }

    @Override
    public Optional<Member> findById(Long memberId) {
        return jpaMemberDao.findById(memberId);
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return jpaMemberDao.findByEmailAndPassword(email, password);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpaMemberDao.existsByEmail(email);
    }

    @Override
    public boolean existsByName(String name) {
        return jpaMemberDao.existsByName(name);
    }
}
