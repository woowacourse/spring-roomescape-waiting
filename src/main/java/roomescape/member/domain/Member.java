package roomescape.member.domain;

public class Member {

    private final Long id;
    private final String name;
    private final String email;
    private final String password;
    private final Role role;

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

    public Member(Long id, Member member) {
        this(id, member.name, member.email, member.password);
    }

    public Member(String name, String email, String password) {
        this(null, name, email, password);
    }

    public boolean isSameMember(Member other) {
        return id.equals(other.id);
    }

    public boolean isAdminUser() {
        return role.equals(Role.ADMIN);
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
}
