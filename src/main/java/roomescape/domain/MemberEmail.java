package roomescape.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import roomescape.exception.member.InvalidMemberEmailPatternException;

@Embeddable
public class MemberEmail {
    private static final Pattern ADDRESS_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");

    @Column(name = "email", unique = true)
    private String address;

    protected MemberEmail() {
    }

    public MemberEmail(String address) {
        validate(address);
        this.address = address;
    }

    private void validate(String address) {
        Matcher matcher = ADDRESS_PATTERN.matcher(address);
        if (!matcher.matches()) {
            throw new InvalidMemberEmailPatternException();
        }
    }

    public String getAddress() {
        return address;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        MemberEmail that = (MemberEmail) o;
        return Objects.equals(address, that.address);
    }

    @Override
    public int hashCode() {
        return Objects.hash(address);
    }
}
