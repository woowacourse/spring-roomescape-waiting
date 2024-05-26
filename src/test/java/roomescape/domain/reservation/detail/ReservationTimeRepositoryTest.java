package roomescape.domain.reservation.detail;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static roomescape.fixture.Fixture.MEMBER_1;
import static roomescape.fixture.Fixture.THEME_1;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.domain.exception.DomainNotFoundException;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.dto.AvailableReservationTimeDto;

@DataJpaTest
class ReservationTimeRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    @DisplayName("이용 가능한 시간들을 조회한다.")
    void findAvailableReservationTimes() {
        // given
        LocalDateTime now = LocalDateTime.of(2024, 4, 8, 10, 0);
        LocalDate date = LocalDate.of(2024, 4, 10);
        Theme theme = themeRepository.save(THEME_1);

        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(9, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        ReservationDetail detail = new ReservationDetail(date, time1, theme);

        Member member = memberRepository.save(MEMBER_1);

        reservationRepository.save(Reservation.create(now, detail, member));

        // when
        List<AvailableReservationTimeDto> responses = reservationTimeRepository
                .findAvailableReservationTimes(date, theme.getId());

        // then
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(responses).hasSize(2);

            softly.assertThat(responses.get(0).id()).isEqualTo(time1.getId());
            softly.assertThat(responses.get(0).startAt()).isEqualTo("09:00");
            softly.assertThat(responses.get(0).alreadyBooked()).isTrue();

            softly.assertThat(responses.get(1).id()).isEqualTo(time2.getId());
            softly.assertThat(responses.get(1).startAt()).isEqualTo("10:00");
            softly.assertThat(responses.get(1).alreadyBooked()).isFalse();
        });
    }

    @Test
    @DisplayName("아이디로 예약 시간을 조회한다.")
    void getById() {
        ReservationTime savedReservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(10, 0)));

        ReservationTime reservationTime = reservationTimeRepository.getById(savedReservationTime.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(reservationTime.getId()).isNotNull();
            softly.assertThat(reservationTime.getStartAt()).isEqualTo("10:00");
        });
    }

    @Test
    @DisplayName("아이디로 예약 시간을 조회하고, 없을 경우 예외를 발생시킨다.")
    void getByIdWhenNotExist() {
        assertThatThrownBy(() -> reservationTimeRepository.getById(-1L))
                .isInstanceOf(DomainNotFoundException.class)
                .hasMessage(String.format("해당 id의 예약 시간이 존재하지 않습니다. (id: %d)", -1L));
    }
}
