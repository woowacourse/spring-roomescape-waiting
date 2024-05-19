package roomescape.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Repository;
import roomescape.domain.Email;
import roomescape.domain.Member;
import roomescape.domain.Password;
import roomescape.repository.jpa.JpaMemberDao;

@Repository
public class JpaMemberRepository implements MemberRepository {
    private final JpaMemberDao jpaMemberDao;

    public JpaMemberRepository(JpaMemberDao jpaMemberDao) {
        this.jpaMemberDao = jpaMemberDao;
    }

    @Override
    public Optional<Member> findByEmailAndEncryptedPassword(String email, String encryptedPassword) {
        return jpaMemberDao.findByEmailAndEncryptedPassword(new Email(email), new Password(encryptedPassword));
    }

    @Override
    public Optional<Member> findById(long id) {
        return jpaMemberDao.findById(id);
    }

    @Override
    public List<Member> findAll() {
        return jpaMemberDao.findAll();
    }
}
