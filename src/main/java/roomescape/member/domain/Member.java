package roomescape.member.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import roomescape.global.exception.ViolationException;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Name name;

    @Embedded
    private Email email;

    @Column(nullable = false, name = "password")
    private String password;

    @Column(nullable = false, name = "role")
    @Enumerated(value = EnumType.STRING)
    private Role role;

    protected Member() {
    }

    public Member(String name, String email, String password, Role role) {
        this(null, name, email, password, role);
    }

    public Member(Long id, Member member) {
        this(id, member.name, member.email, member.password, member.role);
    }

    public Member(Long id, String name, String email, String password, Role role) {
        this(id, new Name(name), new Email(email), password, role);
    }

    public Member(Long id, Name name, Email email, String password, Role role) {
        validatePassword(password);
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new ViolationException("비밀번호는 비어있을 수 없습니다.");
        }
    }

    public boolean hasSamePassword(String password) {
        return this.password.equals(password);
    }

    public boolean isAdmin() {
        return this.role == Role.ADMIN;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getValue();
    }

    public Email getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public Role getRole() {
        return role;
    }
}
