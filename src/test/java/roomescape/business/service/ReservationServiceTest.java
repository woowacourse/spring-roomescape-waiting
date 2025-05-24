package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.business.domain.Member;
import roomescape.business.domain.Reservation;
import roomescape.business.domain.ReservationTime;
import roomescape.business.domain.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidDateAndTimeException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.persistence.repository.WaitInfoRepository;
import roomescape.presentation.dto.ReservationMineResponse;
import roomescape.presentation.dto.ReservationResponse;

@DataJpaTest
public class ReservationServiceTest {

    private static final LocalDate MAX_DATE_FIXTURE = LocalDate.of(9999, 12, 31);

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;

    @Autowired
    public ReservationServiceTest(
            final MemberRepository memberRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final ThemeRepository themeRepository,
            final ReservationRepository reservationRepository,
            final WaitInfoRepository waitInfoRepository
    ) {
        this.reservationService = new ReservationService(reservationRepository,
                memberRepository,
                reservationTimeRepository,
                themeRepository,
                waitInfoRepository);
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
    }

    @Test
    @DisplayName("방탈출 예약 요청 객체로 방탈출 예약을 저장한다")
    void insert() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        // when
        final ReservationResponse reservationResponse = reservationService.insert(
                member.getId(),
                theme.getId(),
                MAX_DATE_FIXTURE,
                time.getId()
        );

        // then
        assertAll(
                () -> assertThat(reservationResponse.id()).isNotNull(),
                () -> assertThat(reservationResponse.memberName()).isEqualTo("후유"),
                () -> assertThat(reservationResponse.themeName()).isEqualTo("테마"),
                () -> assertThat(reservationResponse.date()).isEqualTo(MAX_DATE_FIXTURE),
                () -> assertThat(reservationResponse.startAt()).isEqualTo("14:00")
        );
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 예약하면 예외가 발생한다")
    void insertWhenNotExistMember() {
        // given
        final Long notExistMemberId = 999L;

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        // when & then
        assertThatThrownBy(() -> reservationService.insert(
                notExistMemberId,
                theme.getId(),
                MAX_DATE_FIXTURE,
                time.getId())
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 방탈출 예약 시간으로 예약하면 예외가 발생한다")
    void insertWhenNotExistReservationTime() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        final Long notExistTimeId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationService.insert(
                member.getId(),
                theme.getId(),
                MAX_DATE_FIXTURE,
                notExistTimeId)
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약하면 예외가 발생한다")
    void insertWhenNotExistTheme() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Long notExistThemeId = 999L;

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        // when & then
        assertThatThrownBy(() -> reservationService.insert(
                member.getId(),
                notExistThemeId,
                MAX_DATE_FIXTURE,
                time.getId())
        ).isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약하려는 방탈출 예약과 동일한 날짜, 시간, 테마가 이미 존재한다면 예외가 발생한다")
    void insertWhenDuplicateDateAndTimeAndTheme() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE, time.getId());

        // when & then
        assertThatThrownBy(() -> reservationService.insert(
                member.getId(),
                theme.getId(),
                MAX_DATE_FIXTURE,
                time.getId())
        ).isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("예약 시간이 현재를 기준으로 과거라면 예외가 발생한다")
    void insertWhenDateAndTimeIsPast() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        final LocalDate pastDate = LocalDate.MIN;

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        // when & then
        assertThatThrownBy(() -> reservationService.insert(member.getId(), theme.getId(), pastDate, time.getId()))
                .isInstanceOf(InvalidDateAndTimeException.class);
    }

    @Test
    @DisplayName("모든 방탈출 예약을 조회한다")
    void findAll() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE, time.getId());
        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE.minusDays(1), time.getId());

        // when
        final List<ReservationResponse> reservationResponses = reservationService.findAll();

        // then
        assertThat(reservationResponses).hasSize(2);
    }

    @Test
    @DisplayName("memberId를 통해 필터링하여 방탈출 예약을 조회한다")
    void filterByMemberId() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);
        final Member member2 = new Member("브라운", "USER", "braun@test.com", "pass");
        memberRepository.save(member2);

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE, time.getId());
        reservationService.insert(member2.getId(), theme.getId(), MAX_DATE_FIXTURE.minusDays(1), time.getId());

        // when
        final List<ReservationResponse> reservationResponses = reservationService.findAllFilter(
                member.getId(),
                null,
                null,
                null
        );

        // then
        assertThat(reservationResponses).hasSize(1);
    }

    @Test
    @DisplayName("themeId를 통해 필터링하여 방탈출 예약을 조회한다")
    void filterByThemeId() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        final Theme theme2 = new Theme("테마2", "설명2", "썸네일2");
        themeRepository.save(theme2);

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE, time.getId());
        reservationService.insert(member.getId(), theme2.getId(), MAX_DATE_FIXTURE.minusDays(1), time.getId());

        // when
        final List<ReservationResponse> reservationResponses = reservationService.findAllFilter(
                null,
                theme.getId(),
                null,
                null
        );

        // then
        assertThat(reservationResponses).hasSize(1);
    }

    @Test
    @DisplayName("시작 날짜와 끝 날짜를 통해 필터링하여 방탈출 예약을 조회한다")
    void filterByDateBetween() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE, time.getId());
        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE.minusDays(1), time.getId());
        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE.minusDays(2), time.getId());
        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE.minusDays(3), time.getId());


        // when
        final List<ReservationResponse> reservationResponses = reservationService.findAllFilter(
                null,
                null,
                MAX_DATE_FIXTURE.minusDays(2),
                MAX_DATE_FIXTURE.minusDays(1)
        );

        // then
        assertThat(reservationResponses).hasSize(2);
    }

    @Test
    @DisplayName("id를 통해 방탈출 예약을 삭제한다")
    void deleteById() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        final ReservationResponse reservationResponse = reservationService.insert(
                member.getId(),
                theme.getId(),
                MAX_DATE_FIXTURE,
                time.getId()
        );
        final Long reservationId = reservationResponse.id();

        // when
        reservationService.deleteById(reservationId);

        // then
        final Optional<Reservation> findReservation = reservationRepository.findById(reservationId);
        assertThat(findReservation).isEmpty();
    }

    @Test
    @DisplayName("id를 통해 예약을 삭제할 때 대상이 없다면 예외가 발생한다")
    void deleteByIdWhenNotExistReservation() {
        // given
        final Long notExistId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationService.deleteById(notExistId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("member id를 통해 내 예약 목록을 조회한다")
    void findByMemberId() {
        // given
        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        final ReservationTime time = new ReservationTime(LocalTime.of(14, 0));
        reservationTimeRepository.save(time);

        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE, time.getId());
        reservationService.insert(member.getId(), theme.getId(), MAX_DATE_FIXTURE.minusDays(1), time.getId());

        //when
        final List<ReservationMineResponse> reservationMineResponses = reservationService.findByMemberId(
                member.getId());

        //then
        assertAll(() -> assertThat(reservationMineResponses).hasSize(2));
    }
}
