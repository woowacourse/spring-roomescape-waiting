package roomescape.reservation.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.system.exception.model.DataDuplicateException;
import roomescape.system.exception.model.NotFoundException;
import roomescape.system.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Sql(scripts = "/truncate.sql", executionPhase = ExecutionPhase.BEFORE_TEST_METHOD)
@Import({ReservationService.class, MemberService.class, ReservationTimeService.class, ThemeService.class})
class ReservationServiceTest {

    @Autowired
    ReservationTimeRepository reservationTimeRepository;
    @Autowired
    ReservationRepository reservationRepository;
    @Autowired
    ThemeRepository themeRepository;
    @Autowired
    MemberRepository memberRepository;
    @Autowired
    private ReservationService reservationService;


    @Test
    @DisplayName("동일한 날짜와 시간과 테마에 예약을 생성하면 예외가 발생한다")
    void duplicateTimeReservationAddFail() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 30)));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));

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
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 30)));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));
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
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(beforeTime.toLocalTime()));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));

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
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(beforeTime.toLocalTime()));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Long NotExistMemberId = 1L;

        // when & then
        assertThatThrownBy(() -> reservationService.addReservation(
                new ReservationRequest(beforeTime.toLocalDate(), reservationTime.getId(), theme.getId()), NotExistMemberId))
                .isInstanceOf(NotFoundException.class);
    }
}
