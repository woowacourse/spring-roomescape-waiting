package roomescape.member.domain;

import jakarta.persistence.EntityManager;

public class MemberFixtures {

    public static Member createAndPersistMember(EntityManager entityManager) {
        Member member = new Member("test@test.com", "password", "테스터", Role.USER);
        entityManager.persist(member);
        return member;
    }
}
