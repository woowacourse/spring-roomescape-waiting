package roomescape.member.domain;

import jakarta.persistence.*;
import org.hibernate.annotations.ColumnDefault;
import roomescape.exception.BadRequestException;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    @ColumnDefault(value = "'USER'")
    private Role role;

    public Member() {
    }

    public Member(Long id, String name, String email, String password, Role role) {
        validateNotNull(name, email, password, role);
        validateNotBlank(name, email, password);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private void validateNotNull(String name, String email, String password, Role role) {
        if (name == null || email == null || password == null || role == null) {
            throw new IllegalArgumentException("멤버의 필드는 null 값이 들어올 수 없습니다.");
        }
    }

    private void validateNotBlank(String name, String email, String password) {
        if (name.isBlank() || email.isBlank() || password.isBlank()) {
            throw new IllegalArgumentException("멤버의 필드는 비어있을 수 없습니다.");
        }
    }

    public Member(Long id, String name, String email, String password) {
        this(id, name, email, password, Role.USER);
    }

    public Member(String name, String email, String password) {
        this(null, name, email, password);
    }

    public boolean isSameMember(Member other) {
        return id.equals(other.id);
    }

    public void validatePassword(String password) {
        if (!this.password.equals(password)) {
            throw new BadRequestException("잘못된 사용자 인증 정보입니다.");
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Member member = (Member) o;

        return id.equals(member.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
