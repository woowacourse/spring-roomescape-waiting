package roomescape.repository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import roomescape.domain.Member;

public class CollectionMemberRepository implements MemberRepository {
    private final List<Member> members;

    public CollectionMemberRepository() {
        this(new ArrayList<>());
    }

    public CollectionMemberRepository(List<Member> members) {
        this.members = members;
    }

    @Override
    public Optional<Member> findByEmailAndEncryptedPassword(String email, String encryptedPassword) {
        return members.stream()
                .filter(member -> email.equals(member.getEmail()))
                .filter(member -> encryptedPassword.equals(member.getEncryptedPassword()))
                .findAny();
    }

    @Override
    public Optional<Member> findById(long id) {
        return members.stream()
                .filter(member -> member.getId() == id)
                .findAny();
    }

    @Override
    public List<Member> findAll() {
        return Collections.unmodifiableList(members);
    }
}
