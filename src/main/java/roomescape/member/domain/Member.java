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
import roomescape.exception.model.RoomEscapeException;
import roomescape.member.exception.MemberExceptionCode;
import roomescape.member.role.MemberRole;
import roomescape.vo.Name;

@Entity
public class Member {

    private static final int NULL_ID = 0;
    private static final Pattern EMAIL_FORM = Pattern.compile("^[_a-z0-9-]+(.[_a-z0-9-]+)*@(?:\\w+\\.)+\\w+$");
    private static final Pattern PASSWORD_FORM = Pattern.compile("^(?=.*\\d)(?=.*[a-z])[a-z0-9]*$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Embedded
    private Name name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    public Member() {
    }

    public Member(long id, Name name, String email, String password, MemberRole role) {
        validateEmailFormat(email);
        validatePassword(password);

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(Name name, String email, String password, MemberRole role) {
        this(NULL_ID, name, email, password, role);
    }

    private static void validateEmailFormat(String email) {
        if (!EMAIL_FORM.matcher(email).matches()) {
            throw new RoomEscapeException(MemberExceptionCode.ILLEGAL_EMAIL_FORM_EXCEPTION);
        }
    }

    private static void validatePassword(String password) {
        if (!PASSWORD_FORM.matcher(password).matches()) {
            throw new RoomEscapeException(MemberExceptionCode.ILLEGAL_PASSWORD_FORM_EXCEPTION);
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
        return password;
    }

    public MemberRole getRole() {
        return role;
    }
}
