package roomescape.domain.reservationtime;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.domain.BaseRepositoryTest;
import roomescape.domain.member.Member;
import roomescape.domain.theme.Theme;
import roomescape.support.fixture.MemberFixture;
import roomescape.support.fixture.ReservationFixture;
import roomescape.support.fixture.ReservationTimeFixture;
import roomescape.support.fixture.ThemeFixture;

class ReservationTimeRepositoryTest extends BaseRepositoryTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Test
    @DisplayName("예약 가능한 시간들을 조회한다.")
    void findAvailableReservationTimes() {
        Member member = save(MemberFixture.user());
        Theme theme = save(ThemeFixture.create());
        ReservationTime nine = save(ReservationTimeFixture.create("09:00"));
        ReservationTime twelve = save(ReservationTimeFixture.create("12:00"));
        ReservationTime seventeen = save(ReservationTimeFixture.create("17:00"));
        ReservationTime twentyOne = save(ReservationTimeFixture.create("21:00"));
        String date = "2024-04-09";
        save(ReservationFixture.create(date, member, twelve, theme));
        save(ReservationFixture.create(date, member, twentyOne, theme));

        List<AvailableReservationTimeDto> availableReservationTimes = reservationTimeRepository
                .findAvailableReservationTimes(LocalDate.parse(date), theme.getId());

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(availableReservationTimes).hasSize(4);

            softly.assertThat(availableReservationTimes.get(0).id()).isEqualTo(nine.getId());
            softly.assertThat(availableReservationTimes.get(0).startAt()).isEqualTo(nine.getStartAt());
            softly.assertThat(availableReservationTimes.get(0).alreadyBooked()).isFalse();

            softly.assertThat(availableReservationTimes.get(1).id()).isEqualTo(twelve.getId());
            softly.assertThat(availableReservationTimes.get(1).startAt()).isEqualTo(twelve.getStartAt());
            softly.assertThat(availableReservationTimes.get(1).alreadyBooked()).isTrue();

            softly.assertThat(availableReservationTimes.get(2).id()).isEqualTo(seventeen.getId());
            softly.assertThat(availableReservationTimes.get(2).startAt()).isEqualTo(seventeen.getStartAt());
            softly.assertThat(availableReservationTimes.get(2).alreadyBooked()).isFalse();

            softly.assertThat(availableReservationTimes.get(3).id()).isEqualTo(twentyOne.getId());
            softly.assertThat(availableReservationTimes.get(3).startAt()).isEqualTo(twentyOne.getStartAt());
            softly.assertThat(availableReservationTimes.get(3).alreadyBooked()).isTrue();
        });
    }

    @Test
    @DisplayName("startAt에 해당하는 예약 시간이 존재하면 true를 반환한다.")
    void existsByValidStartAt() {
        save(ReservationTimeFixture.create("10:00"));

        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(10, 0))).isTrue();
    }

    @Test
    @DisplayName("startAt에 해당하는 예약 시간이 존재하지 않으면 false를 반환한다.")
    void existsByInvalidStartAt() {
        save(ReservationTimeFixture.create("10:00"));

        assertThat(reservationTimeRepository.existsByStartAt(LocalTime.of(11, 0))).isFalse();
    }
}
