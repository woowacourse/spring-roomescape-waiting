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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Entity
@Table(name = "users")
public class User {

    @EmbeddedId
    private final Id id = Id.issue();
    private UserRole userRole;
    @Embedded
    private UserName name;
    @Embedded
    private Email email;
    @Embedded
    private Password password;

    public User(final String name, final String email, final String rawPassword) {
        this.userRole = UserRole.USER;
        this.name = new UserName(name);
        this.email = new Email(email);
        this.password = Password.encode(rawPassword);
    }

    public static User admin(final String name, final String email, final String password) {
        User user = new User(name, email, password);
        user.userRole = UserRole.ADMIN;
        return user;
    }

    public boolean isPasswordCorrect(final String password) {
        return this.password.matches(password);
    }

    public boolean isSameUser(final String userId) {
        return id.isSameId(userId);
    }
}
