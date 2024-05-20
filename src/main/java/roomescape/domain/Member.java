package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.hibernate.proxy.HibernateProxy;
import roomescape.exception.ErrorType;
import roomescape.exception.clienterror.EmptyValueNotAllowedException;
import roomescape.exception.clienterror.InvalidClientFieldWithValueException;

@Entity
@Table(name = "member")
public class Member {
    private static Pattern EMAIL_REGEX = Pattern.compile("^[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, name = "email")
    private String email;
    @Embedded
    @Column(nullable = false)
    private Password password;
    @Column(nullable = false, name = "name")
    private String name;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, name = "role")
    private Role role;

    protected Member() {
    }

    public Member(final String email, final Password password, final String name, final Role role) {
        this(null, email, password, name, role);
    }

    public Member(final Long id, final String email, final Password password, final String name, final Role role) {
        validEmail(email);
        validEmpty("name", name);
        this.id = id;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
    }

    private void validEmail(String email) {
        validEmpty("email", email);
        Matcher matcher = EMAIL_REGEX.matcher(email);
        if (!matcher.matches()) {
            throw new InvalidClientFieldWithValueException(ErrorType.INVALID_DATA_TYPE, "email", email);
        }
    }

    private void validEmpty(final String fieldName, final String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new EmptyValueNotAllowedException(fieldName);
        }
    }

    public boolean isAdmin() {
        return role.isAdmin();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public Password getPassword() {
        return password;
    }

    @Override
    public final boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null) {
            return false;
        }
        Class<?> oEffectiveClass = o instanceof HibernateProxy
                ? ((HibernateProxy) o).getHibernateLazyInitializer().getPersistentClass()
                : o.getClass();
        Class<?> thisEffectiveClass = this instanceof HibernateProxy
                ? ((HibernateProxy) this).getHibernateLazyInitializer().getPersistentClass()
                : this.getClass();
        if (thisEffectiveClass != oEffectiveClass) {
            return false;
        }
        final Member member = (Member) o;
        return getId() != null && Objects.equals(getId(), member.getId());
    }

    @Override
    public final int hashCode() {
        return this instanceof HibernateProxy ? ((HibernateProxy) this).getHibernateLazyInitializer()
                .getPersistentClass()
                .hashCode() : getClass().hashCode();
    }
}
