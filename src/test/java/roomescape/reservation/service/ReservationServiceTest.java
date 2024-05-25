package roomescape.reservation.service;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.jdbc.Sql.ExecutionPhase;
import roomescape.global.exception.model.DataDuplicateException;
import roomescape.global.exception.model.NotFoundException;
import roomescape.global.exception.model.ValidateException;
import roomescape.member.domain.Member;
import roomescape.member.domain.Role;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.service.MemberService;
import roomescape.reservation.domain.MemberReservation;
import roomescape.reservation.domain.ReservationStatus;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.repository.MemberReservationRepository;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.domain.repository.ReservationTimeRepository;
import roomescape.reservation.dto.request.ReservationRequest;
import roomescape.reservation.dto.response.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.service.ThemeService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

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
    @Autowired
    private MemberReservationRepository memberReservationRepository;


    @Test
    @DisplayName("동일한 날짜와 시간과 테마에 예약을 생성하면 예외가 발생한다")
    void duplicateTimeReservationAddFail() {
        // given
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(LocalTime.of(12, 30)));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));

        // when & then
        reservationService.addMemberReservation(
                new ReservationRequest(LocalDate.now().plusDays(1L), reservationTime.getId(), theme.getId()), member.getId(), ReservationStatus.RESERVED);

        assertThatThrownBy(() -> reservationService.addMemberReservation(
                new ReservationRequest(LocalDate.now().plusDays(1L), reservationTime.getId(), theme.getId()), member.getId(), ReservationStatus.RESERVED))
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
        assertThatThrownBy(() -> reservationService.addMemberReservation(
                new ReservationRequest(beforeDate, reservationTime.getId(), theme.getId()), member.getId(), ReservationStatus.RESERVED))
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
        assertThatThrownBy(() -> reservationService.addMemberReservation(
                new ReservationRequest(beforeTime.toLocalDate(), reservationTime.getId(), theme.getId()), member.getId(), ReservationStatus.RESERVED))
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
        assertThatThrownBy(() -> reservationService.addMemberReservation(
                new ReservationRequest(beforeTime.toLocalDate(), reservationTime.getId(), theme.getId()), NotExistMemberId, ReservationStatus.RESERVED))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약 상태에 따른 예약정보를 조회한다.")
    void findReservedReservations() {
        // given
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1L);
        ReservationTime reservationTime = reservationTimeRepository.save(new ReservationTime(tomorrow.toLocalTime()));
        Theme theme1 = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Theme theme2 = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Member member = memberRepository.save(new Member("name", "email@email.com", "password", Role.MEMBER));

        reservationService.addMemberReservation(new ReservationRequest(tomorrow.toLocalDate(), reservationTime.getId(), theme1.getId()), member.getId(), ReservationStatus.RESERVED);
        reservationService.addMemberReservation(new ReservationRequest(tomorrow.toLocalDate(), reservationTime.getId(), theme2.getId()), member.getId(), ReservationStatus.RESERVED);
        reservationService.addMemberReservation(new ReservationRequest(tomorrow.toLocalDate(), reservationTime.getId(), theme2.getId()), member.getId(), ReservationStatus.WAITING);

        // when
        List<MemberReservation> reservedReservations = memberReservationRepository.findByStatus(ReservationStatus.RESERVED);
        List<MemberReservation> waitingReservations = memberReservationRepository.findByStatus(ReservationStatus.WAITING);

        // then
        Assertions.assertThat(reservedReservations).hasSize(2);
        Assertions.assertThat(waitingReservations).hasSize(1);
    }

    @Test
    @DisplayName("첫 번째 순서로 대기 중인 예약 대기 정보들을 조회한다.")
    void findFirstOrderWaitingReservations() {
        // given
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1L);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(tomorrow.toLocalTime()));
        Theme theme1 = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Theme theme2 = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Member member1 = memberRepository.save(new Member("name1", "email1@email.com", "password", Role.MEMBER));
        Member member2 = memberRepository.save(new Member("name2", "email2@email.com", "password", Role.MEMBER));
        Member member3 = memberRepository.save(new Member("name3", "email3@email.com", "password", Role.MEMBER));

        reservationService.addMemberReservation(new ReservationRequest(tomorrow.toLocalDate(), time.getId(), theme1.getId()), member1.getId(), ReservationStatus.RESERVED);
        reservationService.addMemberReservation(new ReservationRequest(tomorrow.toLocalDate(), time.getId(), theme1.getId()), member2.getId(), ReservationStatus.WAITING);
        reservationService.addMemberReservation(new ReservationRequest(tomorrow.toLocalDate(), time.getId(), theme2.getId()), member2.getId(), ReservationStatus.RESERVED);
        reservationService.addMemberReservation(new ReservationRequest(tomorrow.toLocalDate(), time.getId(), theme2.getId()), member3.getId(), ReservationStatus.WAITING);

        // when
        List<ReservationResponse> firstOrderWaitingReservations = reservationService.findFirstOrderWaitingReservations().reservations();

        // then
        Assertions.assertThat(firstOrderWaitingReservations).hasSize(2);
        Assertions.assertThat(firstOrderWaitingReservations.get(0).member().id()).isEqualTo(member2.getId());
        Assertions.assertThat(firstOrderWaitingReservations.get(1).member().id()).isEqualTo(member3.getId());
    }

    @Test
    @DisplayName("예약 대기 요청을 수락하면, 같은 날짜/시간/테마에 이미 예약이 있어도 가능한 것으로 판단하고 하나 더 받는다.")
    void acceptReservationWaiting() {
        // given
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1L);
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(tomorrow.toLocalTime()));
        Theme theme1 = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        Member member1 = memberRepository.save(new Member("name1", "email1@email.com", "password", Role.MEMBER));
        Member member2 = memberRepository.save(new Member("name2", "email2@email.com", "password", Role.MEMBER));

        reservationService.addMemberReservation(new ReservationRequest(tomorrow.toLocalDate(), time.getId(), theme1.getId()), member1.getId(), ReservationStatus.RESERVED);
        ReservationResponse waitingReservation = reservationService.addMemberReservation(new ReservationRequest(tomorrow.toLocalDate(), time.getId(), theme1.getId()), member2.getId(), ReservationStatus.WAITING);

        // when
        reservationService.approveWaitingReservation(waitingReservation.id());

        // then
        List<ReservationResponse> reservedReservations = reservationService.findReservationsByStatus(ReservationStatus.RESERVED).reservations();
        Assertions.assertThat(reservedReservations).hasSize(2);
        Assertions.assertThat(reservedReservations.contains(waitingReservation));
    }

    @Test
    @DisplayName("이미 예약이 존재하는 날짜/시간/테마에 예약 생성을 수행하면 예외가 발생한다.")
    void validateDateTimeThemeDuplication() {
        // given
        Member member1 = memberRepository.save(new Member("이름", "member1@admin.com", "12341234", Role.MEMBER));

        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(17, 30)));
        Theme theme = themeRepository.save(new Theme("테마명", "설명", "썸네일URL"));
        LocalDate tomorrow = LocalDate.now().plusDays(1L);

        reservationService.addMemberReservation(new ReservationRequest(tomorrow, time.getId(), theme.getId()), member1.getId(), ReservationStatus.RESERVED);

        Member member2 = memberRepository.save(new Member("이름", "member2@admin.com", "12341234", Role.MEMBER));

        // when & then
        Assertions.assertThatThrownBy(() -> reservationService.addMemberReservation(new ReservationRequest(tomorrow, time.getId(), theme.getId()), member2.getId(), ReservationStatus.RESERVED))
                .isInstanceOf(DataDuplicateException.class);
    }
}
