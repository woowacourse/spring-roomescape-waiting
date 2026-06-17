package roomescape.domain.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.support.exception.BadRequestException;
import roomescape.support.exception.errors.UserErrors;

@Table(name = "users")
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    private static final int MAX_NAME_LENGTH = 10;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    private User(Long id, String name) {
        validate(name);
        this.id = id;
        this.name = name;
    }

    public static User createWithoutId(String name) {
        return new User(null, name);
    }

    public static User createWithId(long id, User user) {
        return of(id, user.getName());
    }

    public static User of(long id, String name) {
        return new User(id, name);
    }

    private static void validate(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException(UserErrors.INVALID_USER_NAME);
        }
        if (name.length() > MAX_NAME_LENGTH) {
            throw new BadRequestException(UserErrors.INVALID_USER_NAME_LENGTH);
        }
    }
}
