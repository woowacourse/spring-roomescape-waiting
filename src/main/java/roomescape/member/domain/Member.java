package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EnumType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.member.exception.MemberException;
import roomescape.member.exception.MemberExceptionInformation;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor
@Entity(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    @Embedded
    private Password password;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Role role;

    public static Member register(String name, String password) {
        validateName(name);
        return new Member(null, name, Password.from(password), Role.MEMBER);
    }

    public static Member load(Long id, String name, String encryptedPassword, Role role) {
        validateName(name);
        return new Member(id, name, Password.load(encryptedPassword), role);
    }

    private static void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new MemberException(MemberExceptionInformation.NAME_IS_NULL);
        }
    }

    public void matchPassword(String inputPassword) {
        password.validateMatches(inputPassword);
    }

    public boolean hasName(String name) {
        return this.name.equals(name);
    }

    public boolean hasSameId(Member member) {
        return member != null && (this == member || getId() != null && getId().equals(member.getId()));
    }
}
