package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.List;
import java.util.regex.Pattern;
import roomescape.exception.BadRequestException;
import roomescape.exception.ExceptionCause;
import roomescape.reservation.domain.Reservation;

@Entity
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(value = EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToMany(mappedBy = "member")
    private List<Reservation> reservations;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,6}$");

    protected Member() {
    }

    public Member(Long id, String name, String email, String password, Role role) {
        validateName(name);
        validateEmail(email);
        validatePassword(email);

        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException(ExceptionCause.MEMBER_PASSWORD_INVALID_INPUT);
        }

        if (password.length() < 10) {
            throw new BadRequestException(ExceptionCause.MEMBER_PASSWORD_INVALID_INPUT);
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException(ExceptionCause.MEMBER_EMAIL_INVALID_INPUT);
        }

        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException(ExceptionCause.MEMBER_EMAIL_INVALID_INPUT);
        }
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException(ExceptionCause.MEMBER_NAME_INVALID_INPUT);
        }

        int nameLength = name.length();
        if (nameLength < 1 || nameLength > 5) {
            throw new BadRequestException(ExceptionCause.MEMBER_NAME_INVALID_INPUT);
        }
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
