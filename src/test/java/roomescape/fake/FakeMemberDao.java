package roomescape.fake;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.dao.MemberDao;
import roomescape.model.Member;

public class FakeMemberDao implements MemberDao {

    private final Map<Long, Member> database = new HashMap<>();
    private final AtomicLong nextId = new AtomicLong(1L);

    @Override
    public Optional<Member> findByEmail(String email) {
        return database.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findAny();
    }

    public Long add(Member member) {
        Long id = nextId.getAndIncrement();
        database.put(id, new Member(
                member.getName(),
                member.getEmail(),
                member.getPassword(),
                member.getRole()
        ));
        return id;
    }

    @Override
    public Optional<Member> findById(Long id) {
        return Optional.ofNullable(database.get(id));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(database.values());
    }
}

