package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import roomescape.dao.ReservationDao;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.user.Member;
import roomescape.exception.AlreadyExistsException;
import roomescape.exception.ExistReservationException;
import roomescape.exception.NotExistException;
import roomescape.fixture.MemberFixture;
import roomescape.fixture.ThemeFixture;
import roomescape.service.dto.input.AvailableReservationTimeInput;
import roomescape.service.dto.input.ReservationInput;
import roomescape.service.dto.input.ReservationTimeInput;
import roomescape.service.dto.output.AvailableReservationTimeOutput;
import roomescape.service.dto.output.MemberCreateOutput;
import roomescape.service.dto.output.ReservationTimeOutput;
import roomescape.service.dto.output.ThemeOutput;
import roomescape.util.DatabaseCleaner;

@SpringBootTest
class ReservationTimeServiceTest {

    @Autowired
    ReservationTimeService reservationTimeService;
    @Autowired
    ReservationDao reservationDao;

    @Autowired
    ReservationService reservationService;

    @Autowired
    ThemeService themeService;

    @Autowired
    MemberService memberService;

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
        assertThatCode(() -> reservationTimeService.createReservationTime(input))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("유효하지 않은 값을 입력하면 예외를 발생한다.")
    void throw_exception_when_input_is_invalid() {
        final ReservationTimeInput input = new ReservationTimeInput("");
        assertThatThrownBy(() -> reservationTimeService.createReservationTime(input))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("존재하지 않는 시간 ID 를 삭제하려 하면 에외를 발생한다.")
    void throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(-1))
                .isInstanceOf(NotExistException.class);
    }

    @Test
    @DisplayName("특정 시간에 대한 예약이 존재하면 그 시간을 삭제하려 할 때 예외를 발생한다.")
    void throw_exception_when_delete_id_that_exist_reservation() {
        final ReservationTimeOutput timeOutput = reservationTimeService.createReservationTime(
                new ReservationTimeInput("10:00"));
        final ThemeOutput themeOutput = themeService.createTheme(ThemeFixture.getInput());
        final MemberCreateOutput memberOutput = memberService.createMember(MemberFixture.getUserCreateInput());

        reservationDao.create(Reservation.from(
                null,
                "2024-04-30",
                ReservationTime.from(timeOutput.id(), timeOutput.startAt()),
                Theme.of(themeOutput.id(), themeOutput.name(), themeOutput.description(), themeOutput.thumbnail()),
                Member.fromMember(memberOutput.id(), memberOutput.name(), memberOutput.email(), memberOutput.password())
        ));

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(timeOutput.id()))
                .isInstanceOf(ExistReservationException.class);
    }

    @Test
    @DisplayName("중복 예약 시간이면 예외를 발생한다.")
    void throw_exception_when_duplicate_reservationTime() {
        reservationTimeService.createReservationTime(new ReservationTimeInput("10:00"));
        final var input = new ReservationTimeInput("10:00");
        assertThatThrownBy(() -> reservationTimeService.createReservationTime(input))
                .isInstanceOf(AlreadyExistsException.class);
    }

    @Test
    @DisplayName("예약 가능한 시간을 조회한다.")
    void get_available_reservationTime() {
        long timeId1 = reservationTimeService.createReservationTime(new ReservationTimeInput("10:00"))
                .id();
        long timeId2 = reservationTimeService.createReservationTime(new ReservationTimeInput("11:00"))
                .id();
        long themeId = themeService.createTheme(ThemeFixture.getInput())
                .id();
        long memberId = memberService.createMember(MemberFixture.getUserCreateInput()).id();
        reservationService.createReservation(new ReservationInput("2025-01-01", timeId2, themeId,memberId));

        List<AvailableReservationTimeOutput> actual = reservationTimeService.getAvailableTimes(
                new AvailableReservationTimeInput(themeId, LocalDate.parse("2025-01-01")));

        assertThat(actual).containsExactly(
                new AvailableReservationTimeOutput(timeId1, "10:00", false),
                new AvailableReservationTimeOutput(timeId2, "11:00", true)
        );
    }
}
