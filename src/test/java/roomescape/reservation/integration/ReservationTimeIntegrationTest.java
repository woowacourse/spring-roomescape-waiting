package roomescape.reservation.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import roomescape.global.error.exception.BadRequestException;
import roomescape.global.error.exception.ConflictException;
import roomescape.global.error.exception.NotFoundException;
import roomescape.member.entity.Member;
import roomescape.member.entity.RoleType;
import roomescape.member.repository.MemberRepository;
import roomescape.reservation.dto.request.ReservationTimeRequest.ReservationTimeCreateRequest;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationTime;
import roomescape.reservation.repository.ReservationRepository;
import roomescape.reservation.repository.ReservationTimeRepository;
import roomescape.reservation.service.ReservationTimeService;
import roomescape.theme.entity.Theme;
import roomescape.theme.repository.ThemeRepository;

@SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ReservationTimeIntegrationTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @Test
    @DisplayName("예약 시간을 생성한다.")
    void createTime() {
        // given
        var request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));

        // when
        var response = reservationTimeService.createTime(request);

        // then
        assertThat(response.id()).isNotNull();
        assertThat(response.startAt()).isEqualTo("10:00");
    }

    @Test
    @DisplayName("운영 시간 이외의 시간을 생성하면 예외가 발생한다.")
    void createTimeWithInvalidTime() {
        // given
        var request = new ReservationTimeCreateRequest(LocalTime.of(9, 0));

        // when & then
        assertThatThrownBy(() -> reservationTimeService.createTime(request))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("운영 시간 이외의 날짜는 예약할 수 없습니다.");
    }

    @Test
    @DisplayName("중복된 시간을 생성하면 예외가 발생한다.")
    void createTimeWithDuplicateTime() {
        // given
        var request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        reservationTimeService.createTime(request);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.createTime(request))
                .isInstanceOf(ConflictException.class)
                .hasMessage("러닝 타임이 겹치는 시간이 존재합니다.");
    }

    @Test
    @DisplayName("모든 예약 시간을 조회한다.")
    void getAllTimes() {
        // given
        var request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        reservationTimeService.createTime(request);

        // when
        var responses = reservationTimeService.getAllTimes();

        // then
        assertThat(responses).hasSize(1);
        var response = responses.getFirst();
        assertThat(response.id()).isNotNull();
        assertThat(response.startAt()).isEqualTo("10:00");
    }

    @Test
    @DisplayName("특정 날짜와 테마에 대한 예약 가능한 시간을 조회한다.")
    void getAvailableTimes() {
        // given
        var member = memberRepository.save(new Member(0L, "미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme(1L, "테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        var date = LocalDate.of(2024, 3, 20);
        var reservation = new Reservation(0L, date, time, theme.getId(), member.getId());
        reservationRepository.save(reservation);

        // when
        var responses = reservationTimeService.getAvailableTimes(date, theme.getId());

        // then
        assertThat(responses).hasSize(1);
        var response = responses.getFirst();
        assertThat(response.id()).isEqualTo(time.getId());
        assertThat(response.startAt()).isEqualTo("10:00");
        assertThat(response.alreadyBooked()).isTrue();
    }

    @Test
    @DisplayName("예약 시간을 삭제한다.")
    void deleteTime() {
        // given
        var request = new ReservationTimeCreateRequest(LocalTime.of(10, 0));
        var response = reservationTimeService.createTime(request);

        // when
        reservationTimeService.deleteTime(response.id());

        // then
        var times = reservationTimeService.getAllTimes();
        assertThat(times).isEmpty();
    }

    @Test
    @DisplayName("존재하지 않는 예약 시간을 삭제하면 예외가 발생한다.")
    void deleteNonExistentTime() {
        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(1L))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("존재하지 않는 id 입니다.");
    }

    @Test
    @DisplayName("예약이 있는 시간을 삭제하면 예외가 발생한다.")
    void deleteTimeWithReservation() {
        // given
        var member = memberRepository.save(new Member(0L, "미소", "miso@email.com", "password", RoleType.USER));
        var theme = themeRepository.save(new Theme(1L, "테마", "설명", "썸네일"));
        var time = reservationTimeRepository.save(new ReservationTime(1L, LocalTime.of(10, 0)));
        var date = LocalDate.of(2024, 3, 20);
        var reservation = new Reservation(0L, date, time, theme.getId(), member.getId());
        reservationRepository.save(reservation);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteTime(time.getId()))
                .isInstanceOf(BadRequestException.class)
                .hasMessage("해당 시간에 예약된 내역이 존재하므로 삭제할 수 없습니다.");
    }
}
