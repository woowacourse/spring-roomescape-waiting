package roomescape.domain.member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    protected Member() {
    }

    public Member(Long id, String name) {
        this.id = id;
        this.name = name;
    }

    public static Member create(String name) {
        return new Member(null, name);
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }
}
