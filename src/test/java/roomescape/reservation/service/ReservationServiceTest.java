package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.global.exception.model.NotFoundException;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.dao.MemberDao;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.service.MemberService;
import roomescape.reservation.dao.ReservationDao;
import roomescape.reservation.dao.ReservationTimeDao;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.theme.dao.ThemeDao;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@JdbcTest
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Import({ReservationTimeDao.class, ThemeDao.class, ReservationService.class, ReservationDao.class, MemberDao.class, MemberService.class})
class ReservationServiceTest {

    @Autowired
    ReservationTimeDao reservationTimeDao;
    @Autowired
    ThemeDao themeDao;
    @Autowired
    MemberDao memberDao;
    @Autowired
    private ReservationService reservationService;

    @Test
    @DisplayName("동일한 날짜와 시간과 테마에 예약을 생성하면 예외가 발생한다")
    void duplicateTimeReservationAddFail() {
        // given
        ReservationTime reservationTime = reservationTimeDao.insert(new ReservationTime(LocalTime.of(12, 30)));
        Theme theme = themeDao.insert(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberDao.insert(new Member("name", "email@email.com", "password", Role.MEMBER));

        // when & then
        reservationService.addReservation(
                new ReservationRequest(LocalDate.now().plusDays(1L), reservationTime.getId(), theme.getId()),
                member.getId());

        assertThatThrownBy(() -> reservationService.addReservation(
                new ReservationRequest(LocalDate.now().plusDays(1L), reservationTime.getId(), theme.getId()), member.getId()))
                .isInstanceOf(DataDuplicateException.class);
    }

    @Test
    @DisplayName("이미 지난 날짜로 예약을 생성하면 예외가 발생한다")
    void beforeDateReservationFail() {
        // given
        ReservationTime reservationTime = reservationTimeDao.insert(new ReservationTime(LocalTime.of(12, 30)));
        Theme theme = themeDao.insert(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberDao.insert(new Member("name", "email@email.com", "password", Role.MEMBER));
        LocalDate beforeDate = LocalDate.now().minusDays(1L);

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(
                new ReservationRequest(beforeDate, reservationTime.getId(), theme.getId()), member.getId()))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("현재 날짜가 예약 당일이지만, 이미 지난 시간으로 예약을 생성하면 예외가 발생한다")
    void beforeTimeReservationFail() {
        // given
        LocalDateTime beforeTime = LocalDateTime.now().minusHours(1L);
        ReservationTime reservationTime = reservationTimeDao.insert(new ReservationTime(beforeTime.toLocalTime()));
        Theme theme = themeDao.insert(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberDao.insert(new Member("name", "email@email.com", "password", Role.MEMBER));

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(
                new ReservationRequest(beforeTime.toLocalDate(), reservationTime.getId(), theme.getId()), member.getId()))
                .isInstanceOf(ValidateException.class);
    }

    @Test
    @DisplayName("존재하지 않는 회원이 예약을 생성하려고 하면 예외를 발생한다.")
    void notExistMemberReservationFail() {
        // given
        LocalDateTime beforeTime = LocalDateTime.now().minusHours(1L);
        ReservationTime reservationTime = reservationTimeDao.insert(new ReservationTime(beforeTime.toLocalTime()));
        Theme theme = themeDao.insert(new Theme("테마명", "설명", "썸네일URL"));
        Long NotExistMemberId = 1L;

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(
                new ReservationRequest(beforeTime.toLocalDate(), reservationTime.getId(), theme.getId()), NotExistMemberId))
                .isInstanceOf(NotFoundException.class);
    }
}
