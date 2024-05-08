package roomescape.model;

import java.util.Objects;

import roomescape.exception.BadRequestException;

public class User {

    private Long id;
    private String name;
    private Role role;
    private String email;
    private String password;

    private User() {
    }

    public User(Long id, String name, Role role, String email, String password) {
        validateNullOrBlank(name, "name");
        this.id = id;
        this.name = name;
        this.role = role;
        this.email = email;
        this.password = password;
    }

    public User(Long id, String name, Role role) {
        this(id, name, role, null, null);
    }

    private void validateNullOrBlank(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new BadRequestException(value, fieldName);
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
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }
        if (object == null || getClass() != object.getClass()) {
            return false;
        }
        User user = (User) object;
        return Objects.equals(getId(), user.getId()) && Objects.equals(getName(), user.getName())
                && getRole() == user.getRole() && Objects.equals(getEmail(), user.getEmail())
                && Objects.equals(getPassword(), user.getPassword());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getRole(), getEmail(), getPassword());
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", role=" + role +
                ", email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
