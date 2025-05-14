package roomescape.reservation.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import roomescape.member.domain.Member;
import roomescape.member.domain.Role;

class ReservationTest {

    @DisplayName("아이디 존재 여부")
    @ParameterizedTest
    @CsvSource(value = {"1,true", "0,false"})
    void test1(long id, boolean expected) {
        // given
        ReservationTime reservationTime = new ReservationTime(1L, LocalTime.now());
        Theme theme = new Theme(1L, "공포", "우테코 공포",
                "https://i.pinimg.com/236x/6e/bc/46/6ebc461a94a49f9ea3b8bbe2204145d4.jpg");
        Member member = new Member("포스티", "test@test.com", "12341234", Role.MEMBER);

        Reservation reservation = new Reservation(id, member, LocalDate.now(), reservationTime, theme);

        // when
        boolean result = reservation.existId();

        // then
        assertThat(result).isEqualTo(expected);
    }
}
