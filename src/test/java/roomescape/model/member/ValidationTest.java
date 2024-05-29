package roomescape.model.member;

import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.model.ReservationTime;
import roomescape.model.theme.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
class ValidationTest {

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @DisplayName("사용자의 이름이 null 또는 비어있는 경우 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_member_name_null_or_blank(String name) {
        Member member = new Member(name, "email@goole.com", "pw", Role.USER);
        assertThatThrownBy(() -> memberRepository.save(member))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @DisplayName("사용자의 이메일이 null 또는 비어있는 경우, 유효하지 않은 형식인 경우 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t", "email", "email#gmail.com"})
    void should_throw_exception_when_member_email_null_or_blank_or_invalid_format(String email) {
        Member member = new Member("name", email, "pw", Role.USER);
        assertThatThrownBy(() -> memberRepository.save(member))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @DisplayName("사용자의 비밀번호가 null 또는 비어있는 경우 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_member_password_null_or_blank(String password) {
        Member member = new Member("name", "email@goole.com", password, Role.USER);
        assertThatThrownBy(() -> memberRepository.save(member))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @DisplayName("테마의 이름이 null 또는 비어있는 경우 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_theme_name_null_or_blank(String name) {
        Theme theme = new Theme(name, "description", "thumbnail");
        assertThatThrownBy(() -> themeRepository.save(theme))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @DisplayName("테마의 설명이 null 또는 비어있는 경우 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_theme_description_null_or_blank(String description) {
        Theme theme = new Theme("name", description, "thumbnail");
        assertThatThrownBy(() -> themeRepository.save(theme))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @DisplayName("테마의 썸네일이 null 또는 비어있는 경우 예외가 발생한다.")
    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {" ", "\n", "\t"})
    void should_throw_exception_when_theme_thumbnail_null_or_blank(String thumbnail) {
        Theme theme = new Theme("name", "description", thumbnail);
        assertThatThrownBy(() -> themeRepository.save(theme))
                .isInstanceOf(ConstraintViolationException.class);
    }

    @DisplayName("예약 시간의 시간 시간이 null인 경우 예외가 발생한다.")
    @Test
    void should_throw_exception_when_time_startAt_null() {
        ReservationTime reservationTime = new ReservationTime(null);
        assertThatThrownBy(() -> reservationTimeRepository.save(reservationTime))
                .isInstanceOf(ConstraintViolationException.class);
    }
}
