package roomescape.domain.member;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
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
    @Embedded
    private Email email;
    @Embedded
    private Password password;

    protected Member() {
    }

    public Member(Long id, MemberName name, Email email, Password password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Member(MemberName name, Email email, Password password) {
        this(null, name, email, password);
    }

    public boolean hasId(long memberId) {
        return id.equals(memberId);
    }

    public boolean matchPassword(Password otherPassword) {
        return password.equals(otherPassword);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name.getName();
    }

    public String getEmail() {
        return email.getAddress();
    }

    public String getPassword() {
        return password.getPassword();
    }
}
