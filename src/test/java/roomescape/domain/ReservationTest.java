package roomescape.domain;

import static roomescape.TestFixture.ADMIN_ZEZE;
import static roomescape.TestFixture.DATE_AFTER_1DAY;
import static roomescape.TestFixture.MEMBER_BROWN;
import static roomescape.TestFixture.RESERVATION_TIME_10AM;
import static roomescape.TestFixture.ROOM_THEME1;
import static roomescape.TestFixture.ROOM_THEME2;

import java.time.LocalDate;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.exception.BadRequestException;
import roomescape.repository.MemberRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ReservationTest {

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        memberRepository.findAll()
                .forEach(member -> memberRepository.deleteById(member.getId()));
    }

    @DisplayName("사용자에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmptyName(Member value) {
        Assertions.assertThatThrownBy(() -> new Reservation(value, DATE_AFTER_1DAY, RESERVATION_TIME_10AM, ROOM_THEME1, Status.CREATED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("사용자에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("날짜에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmptyDate(LocalDate value) {
        Assertions.assertThatThrownBy(() -> new Reservation(MEMBER_BROWN, value, RESERVATION_TIME_10AM, ROOM_THEME1, Status.CREATED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("날짜에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("시간에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmptyTime(ReservationTime value) {
        Assertions.assertThatThrownBy(() -> new Reservation(MEMBER_BROWN, DATE_AFTER_1DAY, value, ROOM_THEME1, Status.CREATED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("시간에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("테마에 null이 들어가면 예외를 발생시킨다.")
    @ParameterizedTest
    @NullSource
    void nullEmptyTheme(RoomTheme value) {
        Assertions.assertThatThrownBy(
                        () -> new Reservation(MEMBER_BROWN, DATE_AFTER_1DAY, RESERVATION_TIME_10AM, value, Status.CREATED))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("테마에 빈값을 입력할 수 없습니다.");
    }

    @DisplayName("중복된 예약이 존재하면 예외를 발생시킨다.")
    @Test
    void duplicatedDateTime() {
        // given
        Reservation reservation = new Reservation(MEMBER_BROWN, DATE_AFTER_1DAY, RESERVATION_TIME_10AM, ROOM_THEME1, Status.CREATED);
        Reservation comparedReservation = new Reservation(MEMBER_BROWN, DATE_AFTER_1DAY, RESERVATION_TIME_10AM, ROOM_THEME1, Status.CREATED);

        // when & then
        Assertions.assertThatThrownBy(() -> reservation.validateDuplication(comparedReservation))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("중복된 예약이 존재합니다.");
    }

    @DisplayName("중복된 예약시간이 존재하지 않으면 예외를 발생시키지 않는다.")
    @Test
    void NotDuplicatedDateTime() {
        // given
        Member member = memberRepository.save(MEMBER_BROWN);
        Member admin = memberRepository.save(ADMIN_ZEZE);

        Reservation reservation = new Reservation(member, DATE_AFTER_1DAY, RESERVATION_TIME_10AM, ROOM_THEME1, Status.CREATED);
        Reservation comparedReservation = new Reservation(admin, DATE_AFTER_1DAY, RESERVATION_TIME_10AM,
                ROOM_THEME2, Status.CREATED);

        // when & then
        Assertions.assertThatCode(() -> reservation.validateDuplication(comparedReservation))
                .doesNotThrowAnyException();
    }
}
