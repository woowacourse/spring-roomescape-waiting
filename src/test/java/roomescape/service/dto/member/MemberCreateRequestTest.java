package roomescape.service.dto.member;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class MemberCreateRequestTest {

    private Validator validator;

    @BeforeEach
    void setUp() {
        ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
        validator = validatorFactory.getValidator();
    }

    @DisplayName("이메일이 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_null_email_input(String email) {
        MemberCreateRequest requestDto = new MemberCreateRequest(email, "1234", "재즈");

        Set<ConstraintViolation<MemberCreateRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("이메일은 반드시 입력되어야 합니다.");
    }

    @DisplayName("이메일 형식이 맞지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @ValueSource(strings = {"jazzjjang", "casc.coms", "dasdasxz"})
    void throw_exception_when_invalid_email_format_input(String email) {
        MemberCreateRequest requestDto = new MemberCreateRequest(email, "1234", "재즈");

        Set<ConstraintViolation<MemberCreateRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("이메일 형식이 맞지 않습니다.");
    }

    @DisplayName("비밀번호가 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_null_password_input(String password) {
        MemberCreateRequest requestDto = new MemberCreateRequest("123@123.com", password, "재즈");

        Set<ConstraintViolation<MemberCreateRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("비밀번호는 반드시 입력되어야 합니다.");
    }

    @DisplayName("이름이 입력되지 않으면 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    void throw_exception_when_null_member_name__input(String name) {
        MemberCreateRequest requestDto = new MemberCreateRequest("123@123.com", "1234", name);

        Set<ConstraintViolation<MemberCreateRequest>> violations = validator.validate(requestDto);

        assertThat(violations).extracting("message").
                containsOnly("이름은 반드시 입력되어야 합니다.");
    }
}
