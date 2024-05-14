package roomescape.domain.member;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
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
    private PlayerName name;
    @Embedded
    @AttributeOverride(name = "address", column = @Column(name = "email"))
    private Email email;
    @Embedded
    private Password password;

    public Member() {
    }

    public Member(Long id, PlayerName name, Email email, Password password) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public Member(PlayerName name, Email email, Password password) {
        this(null, name, email, password);
    }

    public Member(String name, String email, String password) {
        this(null, new PlayerName(name), new Email(email), new Password(password));
    }

    public Member withId(long id) {
        return new Member(id, name, email, password);
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
