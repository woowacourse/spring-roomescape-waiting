package roomescape.domain;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "name", nullable = false))
    private Name name;
    @Column(nullable = false)
    @Enumerated(value = EnumType.STRING)
    private Role role;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "email", nullable = false))
    private Email email;
    @Embedded
    @AttributeOverride(name = "value", column = @Column(name = "password", nullable = false))
    private Password password;

    public Member() {
    }

    public Member(long id, Member member) {
        this(id, member.getName(), member.getRole(), member.email, member.password);
    }

    public Member(Long id, Name name, Role role, Email email, Password password) {
        this(new LoginMember(id, name, role), email, password);
    }

    public Member(LoginMember loginMember, Email email, Password password) {
        this.id = loginMember.getId();
        this.name = loginMember.getName();
        this.role = loginMember.getRole();
        this.email = email;
        this.password = password;
    }

    public LoginMember getLoginMember() {
        return new LoginMember(id, name, role);
    }

    public Role getRole() {
        return role;
    }

    public long getId() {
        return id;
    }

    public Name getName() {
        return name;
    }

    public Email getEmail() {
        return email;
    }

    public Password getPassword() {
        return password;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", email=" + email +
                ", password='" + password + '\'' +
                '}';
    }
}
