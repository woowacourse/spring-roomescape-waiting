package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.util.regex.Pattern;
import roomescape.global.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;
import roomescape.member.role.MemberRole;
import roomescape.name.domain.Name;

@Entity
public class Member {

    private static final String DEFAULT_NAME = "어드민";
    private static final Pattern EMAIL_FORM = Pattern.compile("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    private Name name;

    @Column(nullable = false)
    private String email;

    @Embedded
    private Password password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    public Member() {
    }

    public Member(long id, Name name, String email, Password password, MemberRole role) {
        validateEmailFormat(email);

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private Member(String name, String email, Password password, MemberRole role) {
        this(0, new Name(name), email, password, role);
    }

    public static Member memberOf(long id, String name, String email, String password, String role) {
        return new Member(id, new Name(name), email, Password.passwordFrom(password),
                MemberRole.findMemberRole(role));
    }

    public static Member saveMemberOf(String email, String password) {
        return new Member(DEFAULT_NAME, email, Password.passwordFrom(password), MemberRole.MEMBER);
    }

    private static void validateEmailFormat(String email) {
        if (!EMAIL_FORM.matcher(email).matches()) {
            throw new RoomEscapeException(MemberExceptionCode.ILLEGAL_EMAIL_FORM_EXCEPTION);
        }
    }

    public long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password.getPassword();
    }

    public MemberRole getRole() {
        return role;
    }
}
