package roomescape.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationInfo;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.ExistReservationException;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ReservationTimeFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.repository.*;
import roomescape.service.dto.input.AvailableReservationTimeInput;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.service.dto.output.AvailableReservationTimeOutput;
import roomescape.util.DatabaseCleaner;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ReservationTimeServiceTest {

    @Autowired
    ReservationTimeService sut;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ReservationInfoRepository reservationInfoRepository;

    @Autowired
    ReservationTimeRepository reservationTimeRepository;

    @Autowired
    ThemeRepository themeRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    DatabaseCleaner databaseCleaner;

    @BeforeEach
    void setUp() {
        databaseCleaner.initialize();
    }

    @Test
    @DisplayName("유효한 값을 입력하면 예외를 발생하지 않는다.")
    void create_reservationTime() {
        final ReservationTimeInput input = new ReservationTimeInput("10:00");
        assertThatCode(() -> sut.createReservationTime(input))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효하지 않은 값을 입력하면 예외를 발생한다.")
    void throw_exception_when_input_is_invalid() {
        final ReservationTimeInput input = new ReservationTimeInput("");
        assertThatThrownBy(() -> sut.createReservationTime(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("특정 시간에 대한 예약이 존재하면 그 시간을 삭제하려 할 때 예외를 발생한다.")
    void throw_exception_when_delete_id_that_exist_reservation() {
        final ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getDomain());
        final Theme theme = themeRepository.save(ThemeFixture.getDomain());
        final Member member = memberRepository.save(MemberFixture.getDomain());
        final ReservationInfo reservationInfo = reservationInfoRepository.save(ReservationInfo.from(
                "2024-04-30",
                reservationTime,
                theme
        ));

        reservationRepository.save(new Reservation(member, reservationInfo));
        final long reservationTimeId = reservationTime.getId();

        assertThatThrownBy(() -> sut.deleteReservationTime(reservationTimeId))
                .isInstanceOf(ExistReservationException.class);
    }

    @Test
    @DisplayName("중복 예약 시간이면 예외를 발생한다.")
    void throw_exception_when_duplicate_reservationTime() {
        final ReservationTime reservationTime = reservationTimeRepository.save(ReservationTimeFixture.getDomain());
        final var input = new ReservationTimeInput(reservationTime.getStartAtAsString());
        assertThatThrownBy(() -> sut.createReservationTime(input))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    @DisplayName("예약 가능한 시간을 조회한다.")
    void get_available_reservationTime() {
        final ReservationTime time1 = reservationTimeRepository.save(ReservationTime.from("10:00"));
        final ReservationTime time2 = reservationTimeRepository.save(ReservationTime.from("11:00"));
        final Theme theme = themeRepository.save(ThemeFixture.getDomain());
        final Member member = memberRepository.save(MemberFixture.getDomain());
        final ReservationInfo reservationInfo = reservationInfoRepository.save(ReservationInfo.from(
                "2025-01-01",
                time1,
                theme
        ));

        reservationRepository.save(new Reservation(member, reservationInfo));


        final List<AvailableReservationTimeOutput> actual = sut.getAvailableTimes(
                new AvailableReservationTimeInput(theme.getId(), LocalDate.parse("2025-01-01")));

        assertThat(actual).containsExactly(
                new AvailableReservationTimeOutput(time1.getId(), "10:00", true),
                new AvailableReservationTimeOutput(time2.getId(), "11:00", false)
        );
    }
}
