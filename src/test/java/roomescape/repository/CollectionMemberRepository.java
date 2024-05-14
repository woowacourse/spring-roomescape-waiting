package roomescape.repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import roomescape.domain.Member;
import roomescape.domain.Members;

public class CollectionMemberRepository implements MemberRepository {

    private final List<Member> members;
    private final AtomicLong index;

    public CollectionMemberRepository() {
        this.members = new ArrayList<>();
        this.index = new AtomicLong(0);
    }

    @Override
    public Members findAll() {
        return new Members(members);
    }

    @Override
    public Optional<Member> findById(long id) {
        return members.stream()
                .filter(member -> member.getId() == id)
                .findFirst();
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        return members.stream()
                .filter(member -> member.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Member save(Member member) {
        Member savedMember = new Member(index.incrementAndGet(), member);
        members.add(savedMember);
        return savedMember;
    }
}
