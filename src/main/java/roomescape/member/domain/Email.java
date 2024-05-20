package roomescape.member.domain;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import roomescape.exceptions.ValidationException;

public record Email(String email) {

    public static final String INVALID_EMAIL_FORMAT = "유효하지 않은 이메일 주소입니다. 예시) xxxxxx@xxx.com";
    private static final int MIN_LOCAL_PART_LENGTH = 5;
    private static final int MAX_LOCAL_PART_LENGTH = 30;
    public static final String INVALID_EMAIL_LENGTH = String.format(
            "도메인 주소 앞 문자열은 %d~%d자리의 영문, 숫자 조합으로 이루어져야 합니다.", MIN_LOCAL_PART_LENGTH, MAX_LOCAL_PART_LENGTH
    );
    private static final int LOCAL_PART_INDEX = 0;
    private static final String EMAIL_FORMAT_REGEX = "^[a-zA-Z0-9]+@[a-zA-Z0-9]+\\.com$";
    private static final Pattern formatPattern = Pattern.compile(EMAIL_FORMAT_REGEX);

    public Email {
        validateLength(email);
        validateFormat(email);
    }

    private void validateLength(String email) {
        String localPart = email.split("@")[LOCAL_PART_INDEX];
        if (localPart.length() < MIN_LOCAL_PART_LENGTH || localPart.length() > MAX_LOCAL_PART_LENGTH) {
            throw new ValidationException(INVALID_EMAIL_LENGTH);
        }
    }

    private void validateFormat(String email) {
        Matcher matcher = formatPattern.matcher(email);
        if (!matcher.matches()) {
            throw new ValidationException(INVALID_EMAIL_FORMAT);
        }
    }
}
