package roomescape.domain.regex;

public class MemberFormat {

    public static final String EMAIL = "[a-zA-Z0-9+-\\_.]+@[a-zA-Z0-9-]+\\.[a-zA-Z0-9-.]+";
    public static final String PASSWORD = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[\\W_]).{8,}$";
}
