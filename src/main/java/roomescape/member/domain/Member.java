package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import roomescape.exception.BadRequestException;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "member")
public class Member {

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberRole role;

    @Column(nullable = false)
    private String password;

    public Member(Long id, String name, String email, MemberRole role, String password) {
        validate(name, email, role, password);
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.password = password;
    }

    public static Member generateWithPrimaryKey(Member member, Long newPrimaryKey) {
        return new Member(newPrimaryKey, member.name, member.email, member.role, member.password);
    }

    public boolean hasSameEmail(String email) {
        return this.email.equals(email);
    }

    public boolean hasSamePassword(String password) {
        return this.password.equals(password);
    }

    private void validate(String name, String email, MemberRole role, String password) {
        if (name == null || name.isBlank()) {
            throw new BadRequestException("이름은 비어있을 수 없습니다.");
        }
        if (email == null || email.isBlank()) {
            throw new BadRequestException("이메일은 비어있을 수 없습니다.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new BadRequestException("유효하지 않은 이메일 형식입니다.");
        }
        if (role == null) {
            throw new BadRequestException("유저 역할은 비어있을 수 없습니다.");
        }
        if (password == null || password.isBlank()) {
            throw new BadRequestException("패스워드는 비어있을 수 없습니다.");
        }
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Member member)) {
            return false;
        }
        return Objects.equals(id, member.id) && Objects.equals(name, member.name)
            && Objects.equals(email, member.email) && role == member.role && Objects.equals(password,
            member.password);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(getId());
    }
}
