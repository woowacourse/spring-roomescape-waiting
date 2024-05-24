package roomescape.service;

import static java.time.LocalDate.now;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import static roomescape.service.fixture.TestMemberFactory.createMember;
import static roomescape.service.fixture.TestReservationFactory.createAcceptReservation;
import static roomescape.service.fixture.TestReservationTimeFactory.createReservationTime;
import static roomescape.service.fixture.TestThemeFactory.createTheme;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;

import roomescape.controller.request.ReservationTimeRequest;
import roomescape.controller.response.IsReservedTimeResponse;
import roomescape.exception.BadRequestException;
import roomescape.exception.DuplicatedException;
import roomescape.exception.NotFoundException;
import roomescape.model.Member;
import roomescape.model.ReservationTime;
import roomescape.model.Theme;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
@Sql(scripts = "/init-data.sql")
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ReservationTimeService reservationTimeService;

    @DisplayName("모든 예약 시간을 반환한다")
    @Test
    void should_return_all_reservation_times() {
        reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(11, 0)));
        reservationTimeRepository.save(new ReservationTime(2L, LocalTime.of(12, 0)));

        List<ReservationTime> reservationTimes = reservationTimeService.findAllReservationTimes();

        assertThat(reservationTimes).hasSize(2);
    }

    @DisplayName("아이디에 해당하는 예약 시간을 반환한다.")
    @Test
    void should_get_reservation_time() {
        reservationTimeRepository.save(createReservationTime(1L, "11:00"));
        reservationTimeRepository.save(createReservationTime(2L, "12:00"));

        ReservationTime reservationTime = reservationTimeService.findReservationTime(2);

        assertThat(reservationTime.getStartAt()).isEqualTo(LocalTime.of(12, 0));
    }

    @DisplayName("예약 시간을 추가한다")
    @Test
    void should_add_reservation_times() {
        reservationTimeService.addReservationTime(new ReservationTimeRequest(LocalTime.of(13, 0)));

        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAll();
        assertThat(allReservationTimes).hasSize(1);
    }

    @DisplayName("예약 시간을 삭제한다")
    @Test
    void should_remove_reservation_times() {
        reservationTimeRepository.save(createReservationTime(1L, "12:00"));
        reservationTimeRepository.save(createReservationTime(2L, "13:00"));

        reservationTimeService.deleteReservationTime(1);

        List<ReservationTime> allReservationTimes = reservationTimeRepository.findAll();
        assertThat(allReservationTimes).hasSize(1);
    }

    @DisplayName("존재하지 않는 시간이면 예외를 발생시킨다.")
    @Test
    void should_throw_exception_when_not_exist_id() {
        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(10000000))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("[ERROR] id(10000000)에 해당하는 예약 시간이 존재하지 않습니다.");
    }

    @DisplayName("존재하는 시간이면 예외가 발생하지 않는다.")
    @Test
    void should_not_throw_exception_when_exist_id() {
        reservationTimeRepository.save(createReservationTime(1L, "12:00"));

        assertThatCode(() -> reservationTimeService.deleteReservationTime(1))
                .doesNotThrowAnyException();
    }

    @DisplayName("특정 시간에 대해 예약이 존재하는데, 그 시간을 삭제하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_exist_reservation_using_time() {
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "12:00"));
        Theme theme = themeRepository.save(createTheme(1L));
        Member member = memberRepository.save(createMember(1L));
        reservationRepository.save(createAcceptReservation(1L, now().plusDays(2), time, theme, member));

        assertThatThrownBy(() -> reservationTimeService.deleteReservationTime(1))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("[ERROR] 해당 시간에 예약이 존재하여 삭제할 수 없습니다.");
    }

    @DisplayName("존재하는 시간을 추가하려 할 때 예외가 발생한다.")
    @Test
    void should_throw_exception_when_add_exist_time() {
        ReservationTime time = reservationTimeRepository.save(createReservationTime(1L, "10:00"));

        ReservationTimeRequest request = new ReservationTimeRequest(time.getStartAt());

        assertThatThrownBy(() -> reservationTimeService.addReservationTime(request))
                .isInstanceOf(DuplicatedException.class)
                .hasMessage("[ERROR] 이미 존재하는 시간입니다.");
    }

    @DisplayName("예약 가능 상태를 담은 시간 정보를 반환한다.")
    @Test
    void should_return_times_with_book_state() {
        ReservationTime reservedTime = reservationTimeRepository.save(createReservationTime(1L, "12:00"));
        ReservationTime notReservedTime = reservationTimeRepository.save(createReservationTime(2L, "13:00"));
        Theme theme = themeRepository.save(createTheme(1L));
        Member member = memberRepository.save(createMember(1L));
        LocalDate reservedDate = now().plusDays(2);
        reservationRepository.save(createAcceptReservation(1L, reservedDate, reservedTime, theme, member));

        List<IsReservedTimeResponse> times = reservationTimeService.getIsReservedTime(reservedDate, 1L);

        assertSoftly(softAssertions -> {
            softAssertions.assertThat(times).hasSize(2);
            softAssertions.assertThat(times).containsOnly(
                    new IsReservedTimeResponse(1L, reservedTime.getStartAt(), true),
                    new IsReservedTimeResponse(2L, notReservedTime.getStartAt(), false));
        });
    }
}
