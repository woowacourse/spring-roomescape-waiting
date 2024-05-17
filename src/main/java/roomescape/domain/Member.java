package roomescape.domain;

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
    private MemberName name;
    @Column(unique = true)
    private String email;
    private String password;
    @Enumerated(EnumType.STRING)
    private MemberRole role;

    public Member() {
    }

    public Member(Long id, MemberName name, String email, String password, MemberRole role) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    public Member(MemberName name, String email, String password, MemberRole role) {
        this(null, name, email, password, role);
    }

    public boolean isDifferentPassword(String target) {
        return !password.equals(target);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public MemberRole getRole() {
        return role;
    }
}
