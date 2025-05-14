package roomescape.repository;

import roomescape.domain.member.Member;

import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

public class FakeMemberRepository implements MemberRepository {

    private final Map<Long, Member> store = new HashMap<>();
    private final AtomicLong sequence = new AtomicLong(1);

    @Override
    public Member save(Member member) {
        if (store.values().stream().anyMatch(m -> m.getEmail().equals(member.getEmail()))) {
            throw new IllegalStateException("[ERROR] 이미 등록된 EMAIL 입니다. " + member.getEmail());
        }

        long id = sequence.getAndIncrement();
        Member newMember = new Member(id, member.getName(), member.getEmail(), member.getRole(), member.getPassword());
        store.put(id, newMember);
        return newMember;
    }

    @Override
    public Optional<Member> findByEmailAndPassword(String email, String password) {
        return store.values().stream()
                .filter(m -> m.getEmail().equals(email) && m.getPassword().equals(password))
                .findFirst();
    }

    @Override
    public Optional<Member> findById(long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<Member> findAll() {
        return new ArrayList<>(store.values());
    }

    @Override
    public int deleteById(long id) {
        return store.remove(id) != null ? 1 : 0;
    }
}
