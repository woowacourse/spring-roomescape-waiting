package roomescape.reservation.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import roomescape.common.exception.AlreadyInUseException;
import roomescape.common.exception.EntityNotFoundException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.repository.MemberDao;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.reservation.dto.ReservationTimeRequest;
import roomescape.reservation.dto.ReservationTimeResponse;
import roomescape.reservation.repository.ReservationDao;
import roomescape.reservation.repository.ReservationTimeDao;
import roomescape.reservation.repository.ThemeDao;

@ActiveProfiles("test")
@JdbcTest
@Import({ReservationDao.class, ReservationTimeDao.class, ThemeDao.class, MemberDao.class,
        ReservationTimeService.class})
class ReservationTimeServiceTest {

    @Autowired
    private ReservationDao reservationDao;
    @Autowired
    private ReservationTimeDao reservationTimeDao;
    @Autowired
    private ThemeDao themeDao;
    @Autowired
    private MemberDao memberDao;
    @Autowired
    private ReservationTimeService reservationTimeService;

    @BeforeEach
    void setUp() {
        reservationTimeService = new ReservationTimeService(reservationTimeDao, reservationDao);
    }

    @DisplayName("모든 시간 정보를 가져온다.")
    @Test
    void test1() {
        // given
        LocalTime localTime1 = LocalTime.of(8, 0);
        LocalTime localTime2 = LocalTime.of(9, 0);
        List<LocalTime> localTimes = List.of(localTime1, localTime2);

        for (LocalTime localTime : localTimes) {
            reservationTimeDao.save(ReservationTime.withoutId(localTime));
        }

        // when
        List<ReservationTimeResponse> result = reservationTimeService.getAll();

        // then
        List<LocalTime> resultTimes = result.stream().map(ReservationTimeResponse::startAt).toList();
        assertThat(resultTimes).containsExactlyInAnyOrderElementsOf(localTimes);
    }

    @DisplayName("정보가 없다면 빈 리스트를 반환한다.")
    @Test
    void test2() {
        // given & when
        List<ReservationTimeResponse> result = reservationTimeService.getAll();

        // then
        assertThat(result).isEmpty();
    }

    @DisplayName("예약 시간을 저장한다.")
    @Test
    void test3() {
        // given
        LocalTime localTime1 = LocalTime.of(8, 0);
        ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(localTime1);

        // when
        ReservationTimeResponse result = reservationTimeService.create(reservationTimeRequest);

        // then
        assertThat(result.id()).isNotNull();
        assertThat(result.startAt()).isEqualTo(localTime1);
    }

    @DisplayName("예약 시간을 삭제한다")
    @Test
    void test4() {
        // given
        ReservationTime saved = reservationTimeDao.save(ReservationTime.withoutId(LocalTime.of(8, 0)));
        Long id = saved.getId();

        // when & then
        assertThatCode(() -> reservationTimeService.delete(id)).doesNotThrowAnyException();
    }

    @DisplayName("예약 시간이 존재하지 않으면 예외를 반환한다.")
    @Test
    void test5() {
        // given
        Long id = 1L;

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(id))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @DisplayName("예약이 존재할 때 예약을 삭제하면 예외를 반환한다.")
    @Test
    void test6() {
        // given
        Theme theme = themeDao.save(Theme.withoutId("테마1", "테마1", "www.m.com"));
        ReservationTime reservationTime = reservationTimeDao.save(ReservationTime.withoutId(LocalTime.of(8, 0)));
        Member member = memberDao.save(new Member("포스티", "test@test.com", "12341234", Role.MEMBER));

        reservationDao.save(Reservation.withoutId(member, LocalDate.now(), reservationTime, theme));
        Long timeId = reservationTime.getId();

        // when & then
        assertThatThrownBy(() -> reservationTimeService.delete(timeId))
                .isInstanceOf(AlreadyInUseException.class);
    }
}
