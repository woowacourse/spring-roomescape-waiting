package roomescape.domain;

public class Password {
    private final String hashValue;
    private final String salt;

    public Password(final String hashValue, final String salt) {
        this.hashValue = hashValue;
        this.salt = salt;
    }

    public String getHashValue() {
        return hashValue;
    }

    public String getSalt() {
        return salt;
    }

    public boolean check(final Password other) {
        return hashValue.equals(other.hashValue) && salt.equals(other.salt);
    }
}
