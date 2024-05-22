package roomescape.domain;

import jakarta.persistence.*;
import org.hibernate.Hibernate;
import org.springframework.lang.NonNull;

import java.util.List;
import java.util.Objects;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @NonNull
    private String name;
    @NonNull
    private String email;
    @NonNull
    private String password;
    @NonNull
    @Enumerated(EnumType.STRING)
    private Role role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<Waiting> waitings;


    public Member(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
    }

    protected Member() {
    }

    public static Member createUser(String name, String email, String password) {
        return new Member(name, email, password, Role.USER);
    }

    public boolean hasWaiting(Waiting otherWaiting) {
        return waitings.stream()
                .anyMatch(waiting -> waiting.isSameReservationWaiting(otherWaiting.getReservation()));
    }

    public void addWaiting(Waiting waiting) {
        waitings.add(waiting);
    }

    public void removeWaiting(Waiting waiting) {
        waitings.remove(waiting);
    }

    public List<Waiting> getWaitings() {
        return waitings;
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
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        Member member = (Member) o;
        return id != null && Objects.equals(id, member.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}
