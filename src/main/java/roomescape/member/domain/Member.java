package roomescape.member.domain;

public class Member {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;

    private Member(Long id, String name, String email, String password) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("이메일은 필수입니다.");
        }
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("비밀번호는 필수입니다.");
        }
        this.id = id;
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public static Member restore(Long id, String name, String email, String password) {
        return new Member(id, name, email, password);
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
}