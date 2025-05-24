package roomescape.member.domain;

import jakarta.persistence.EntityManager;

public class MemberFixtures {

    private static final String DEFAULT_EMAIL = "test@test.com";
    private static final String DEFAULT_PASSWORD = "password";
    private static final String DEFAULT_NAME = "이름";
    private static final Role DEFAULT_ROLE = Role.USER;

    public static Member persistUserMember(EntityManager entityManager, String email, String password, String name,
                                           Role role) {
        Member member = new Member(email, password, name, role);
        entityManager.persist(member);
        return member;
    }

    public static Member persistUserMember(EntityManager entityManager, String email) {
        return persistUserMember(entityManager, email, DEFAULT_PASSWORD, DEFAULT_NAME, DEFAULT_ROLE);
    }

    public static Member persistUserMember(EntityManager entityManager) {
        return persistUserMember(entityManager, DEFAULT_EMAIL, DEFAULT_PASSWORD, DEFAULT_NAME, DEFAULT_ROLE);
    }
}
