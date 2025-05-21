package roomescape.reservation.unit.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.ReservationTimeCreateRequest;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@DataJpaTest
class ReservationTimeServiceTest {

    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @BeforeEach
    void setUp() {
        reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);
    }

    @Test
    @DisplayName("시간을 생성한다.")
    void createTime() {
        // given
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);

        // when
        var response = reservationTimeService.createTime(request);

        // then
        assertThat(response.startAt()).isEqualTo(startAt);
    }

    @Test
    @DisplayName("운영 시간 이외의 시간을 생성하면 예외가 발생한다.")
    void createTimeWithInvalidTime() {
        // given
        var startAt = LocalTime.of(9, 0);
        var request = new ReservationTimeCreateRequest(startAt);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.createTime(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("운영 시간 이외의 날짜는 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("중복된 시간을 생성하면 예외가 발생한다.")
    void createTimeWithDuplicateTime() {
        // given
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);
        reservationTimeService.createTime(request);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.createTime(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("러닝 타임이 겹치는 시간이 존재합니다.");
    }

    @Test
    @DisplayName("모든 시간을 조회한다.")
    void getAllTimes() {
        // given
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);
        reservationTimeService.createTime(request);

        // when
        var responses = reservationTimeService.getAllTimes();

        // then
        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(responses.getFirst().startAt()).isEqualTo(startAt.toString())
        );
    }

    @Test
    @DisplayName("특정 날짜와 테마에 대한 가능한 시간을 조회한다.")
    void getAvailableTimes() {
        // given
        var date = LocalDate.of(2024, 3, 20);
        var themeId = 1L;
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);
        reservationTimeService.createTime(request);

        // when
        var responses = reservationTimeService.getAvailableTimes(date, themeId);

        // then
        var response = responses.getFirst();
        assertAll(
                () -> assertThat(responses).hasSize(1),
                () -> assertThat(response.startAt()).isEqualTo(startAt.toString()),
                () -> assertThat(response.alreadyBooked()).isFalse()
        );
    }

    @Test
    @DisplayName("시간을 삭제한다.")
    void deleteTime() {
        // given
        var startAt = LocalTime.of(10, 0);
        var request = new ReservationTimeCreateRequest(startAt);
        var response = reservationTimeService.createTime(request);

        // when
        reservationTimeService.deleteTime(response.id());

        // then
        var times = reservationTimeService.getAllTimes();
        assertThat(times).isEmpty();
    }

    @Test
    @DisplayName("예약이 있는 시간을 삭제하면 예외가 발생한다.")
    void deleteTimeWithReservation() {
        // given
        var startAt = LocalTime.of(10, 0);
        var timeRequest = new ReservationTimeCreateRequest(startAt);
        var response = reservationTimeService.createTime(timeRequest);
        var theme = themeRepository.save(new Theme("테마1", "테마1 설명", "테마1 썸네일"));
        var member = memberRepository.save(new Member("미소", "miso@email.com", "1234", RoleType.USER));

        var time = new ReservationTime(response.id(), startAt);
        var reservation = new Reservation(LocalDate.now(), time, theme, member);
        reservationRepository.save(reservation);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(response.id()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 시간에 예약된 내역이 존재하므로 삭제할 수 없습니다.");
    }
}
