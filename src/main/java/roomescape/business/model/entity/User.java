package roomescape.business.model.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import roomescape.business.model.vo.Email;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.Password;
import roomescape.business.model.vo.UserName;
import roomescape.business.model.vo.UserRole;

@ToString(exclude = "password")
@EqualsAndHashCode(of = "id")
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
@Getter
@Entity
@Table(name = "users")
public class User {

    @EmbeddedId
    private final Id id = Id.issue();
    private final UserRole userRole;
    @Embedded
    private final UserName name;
    @Embedded
    private final Email email;
    @Embedded
    private final Password password;

    private User(final UserRole userRole, final String name, final String email, final String password) {
        this.userRole = userRole;
        this.name = new UserName(name);
        this.email = new Email(email);
        this.password = Password.encode(password);
    }

    public static User member(final String name, final String email, final String password) {
        return new User(UserRole.USER, name, email, password);
    }

    public static User admin(final String name, final String email, final String password) {
        return new User(UserRole.ADMIN, name, email, password);
    }

    public boolean isPasswordCorrect(final String password) {
        return this.password.matches(password);
    }

    public boolean isSameUser(final String userId) {
        return id.isSameId(userId);
    }
}
