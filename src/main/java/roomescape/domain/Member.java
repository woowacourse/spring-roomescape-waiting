package roomescape.domain;

import static roomescape.domain.Role.MEMBER;
import static roomescape.exception.ExceptionType.EMPTY_NAME;
import static roomescape.exception.ExceptionType.INVALID_EMAIL_FORMAT;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import roomescape.exception.RoomescapeException;

public class Member {
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[_A-Za-z0-9-+]+(\\.[_A-Za-z0-9-]+)*@" +
            "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$");
    //Todo 비밀 번호 형태인지 검증하는 역할을 별도의 도메인으로 분리 : Password 클래스 생성?
    private static final Pattern SHA_256_PATTERN = Pattern.compile("[a-fA-F0-9]{64}");
    private final Long id;
    private final String name;
    private final String email;
    private final String encryptedPassword;
    private final Role role;

    public Member(String name, String email, String encryptedPassword) {
        this(null, name, email, encryptedPassword, MEMBER);
    }

    public Member(Long id, String name, String email, String encryptedPassword, Role role) {
        validateName(name);
        validateEmail(email);
        validateEncryptedPassword(encryptedPassword);
        this.id = id;
        this.name = name;
        this.email = email;
        this.encryptedPassword = encryptedPassword;
        this.role = role;
    }

    private void validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new RoomescapeException(EMPTY_NAME);
        }
    }

    private void validateEmail(String email) {
        if (email == null) {
            throw new RoomescapeException(INVALID_EMAIL_FORMAT);
        }
        Matcher matcher = EMAIL_PATTERN.matcher(email);
        if (!matcher.matches()) {
            throw new RoomescapeException(INVALID_EMAIL_FORMAT);
        }
    }

    //Todo : 클라이언트로 전달하고 싶지 않은 예외 상황 깔끔하게 분리
    private void validateEncryptedPassword(String encryptedPassword) {
        String errorMessage = "암호화된 비밀번호로 생성하세요!";
        if (encryptedPassword == null) {
            throw new IllegalArgumentException(errorMessage);
        }
        Matcher matcher = SHA_256_PATTERN.matcher(encryptedPassword);
        if (!matcher.matches()) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    public Member(Long id, String name, String email, String encryptedPassword) {
        this(id, name, email, encryptedPassword, MEMBER);
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

    public String getEncryptedPassword() {
        return encryptedPassword;
    }

    public Role getRole() {
        return role;
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        Member member = (Member) o;

        return Objects.equals(id, member.id);
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", encryptedPassword='" + encryptedPassword + '\'' +
                ", role=" + role +
                '}';
    }
}
