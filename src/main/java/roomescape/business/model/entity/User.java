package roomescape.business.model.entity;

import jakarta.persistence.Embedded;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;
import roomescape.business.model.vo.*;

@ToString(exclude = "password")
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode(of = "id")
@Getter
@Entity
@Table(name = "users")
public class User {

    @EmbeddedId
    private final Id id;
    private UserRole userRole;
    @Embedded
    private UserName name;
    @Embedded
    private Email email;
    @Embedded
    private Password password;

    protected User() {
        id = Id.issue();
    }

    public static User create(final String name, final String email, final String password) {
        return new User(Id.issue(), UserRole.USER, new UserName(name), new Email(email), Password.encode(password));
    }

    public static User restore(final String id, final String userRole, final String name, final String email, final String password) {
        return new User(Id.create(id), UserRole.valueOf(userRole), new UserName(name), new Email(email), Password.plain(password));
    }

    public boolean isPasswordCorrect(final String password) {
        return this.password.matches(password);
    }

    public boolean isSameUser(final String userId) {
        return id.isSameId(userId);
    }
}
