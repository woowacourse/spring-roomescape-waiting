package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;
import roomescape.business.domain.Reservation;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidDateAndTimeException;
import roomescape.exception.NotFoundException;
import roomescape.infrastructure.repository.MemberRepository;
import roomescape.infrastructure.repository.ReservationRepository;
import roomescape.infrastructure.repository.ReservationTimeRepository;
import roomescape.infrastructure.repository.ThemeRepository;
import roomescape.infrastructure.repository.WaitingRepository;
import roomescape.presentation.dto.LoginMember;
import roomescape.presentation.dto.ReservationMineResponse;
import roomescape.presentation.dto.ReservationRequest;
import roomescape.presentation.dto.ReservationResponse;
import roomescape.util.CurrentUtil;

@SpringBootTest
@Transactional
@Sql("classpath:data-reservationService.sql")
public class ReservationServiceTest {

    private static final LocalDate MAX_DATE_FIXTURE = LocalDate.of(9999, 12, 31);

    @Autowired
    private ReservationService reservationService;
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private WaitingRepository waitingRepository;
    @Autowired
    private QueryService queryService;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private ThemeRepository themeRepository;
    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @TestConfiguration
    static class TestConfig {
        @Bean
        public CurrentUtil currentUtil() {
            return new CurrentUtil() {
                @Override
                public LocalDate getCurrentDate() {
                    return LocalDate.of(2025, 5, 10);
                }

                @Override
                public LocalDateTime getCurrentDateTime() {
                    return LocalDateTime.of(2025, 5, 10, 12, 0);
                }
            };
        }
    }

    // data-reservationService.sql
    private final Long memberId = 100L;
    private final Long timeId = 100L;
    private final Long themeId = 100L;

    @Test
    @DisplayName("방탈출 예약 요청 객체로 방탈출 예약을 저장한다")
    void insert() {
        // when
        final ReservationResponse reservationResponse = reservationService.insert(
                new ReservationRequest(MAX_DATE_FIXTURE, memberId, timeId, themeId)
        );

        // then
        assertAll(
                () -> assertThat(reservationResponse.date()).isEqualTo(MAX_DATE_FIXTURE),
                // member
                () -> assertThat(reservationResponse.member()
                        .id()).isEqualTo(memberId),
                () -> assertThat(reservationResponse.member()
                        .name()).isEqualTo("kim"),
                () -> assertThat(reservationResponse.member()
                        .email()).isEqualTo("email@test.com"),
                // reservation_time
                () -> assertThat(reservationResponse.time()
                        .id()).isEqualTo(timeId),
                () -> assertThat(reservationResponse.time()
                        .startAt()).isEqualTo("14:00"),
                // theme
                () -> assertThat(reservationResponse.theme()
                        .id()).isEqualTo(themeId),
                () -> assertThat(reservationResponse.theme()
                        .name()).isEqualTo("평범"),
                () -> assertThat(reservationResponse.theme()
                        .description()).isEqualTo("평범한 테마입니다.")
        );
    }

    @Test
    @DisplayName("존재하지 않는 사용자로 예약하면 예외가 발생한다")
    void insertWhenNotExistMember() {
        // given
        final Long notExistMemberId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationService.insert(
                new ReservationRequest(MAX_DATE_FIXTURE, notExistMemberId, timeId, themeId)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 방탈출 예약 시간으로 예약하면 예외가 발생한다")
    void insertWhenNotExistReservationTime() {
        // given
        final Long notExistTimeId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationService.insert(
                new ReservationRequest(MAX_DATE_FIXTURE, memberId, notExistTimeId, themeId)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약하면 예외가 발생한다")
    void insertWhenNotExistTheme() {
        // given
        final Long notExistThemeId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationService.insert(
                new ReservationRequest(MAX_DATE_FIXTURE, memberId, timeId, notExistThemeId)))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약하려는 방탈출 예약과 동일한 날짜, 시간, 테마가 이미 존재한다면 예외가 발생한다")
    void insertWhenDuplicateDateAndTimeAndTheme() {
        // given
        reservationService.insert(new ReservationRequest(MAX_DATE_FIXTURE, memberId, timeId, themeId));

        // when & then
        assertThatThrownBy(
                () -> reservationService.insert(new ReservationRequest(MAX_DATE_FIXTURE, memberId, timeId, themeId)))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("예약 시간이 현재를 기준으로 과거라면 예외가 발생한다")
    void insertWhenDateAndTimeIsPast() {
        // given
        final LocalDate pastDate = LocalDate.MIN;

        // when & then
        assertThatThrownBy(() -> reservationService.insert(new ReservationRequest(pastDate, memberId, timeId, themeId)))
                .isInstanceOf(InvalidDateAndTimeException.class);
    }

    @Test
    @DisplayName("모든 방탈출 예약을 조회한다")
    void findAll() {
        // given
        // sql-data-reservationService.sql
        // 4개의 방탈출 예약이 존재한다.

        // when
        final List<ReservationResponse> reservationResponses = reservationService.findAll();

        // then
        assertThat(reservationResponses).hasSize(4);
    }

    @Test
    @DisplayName("모든 방탈출 예약을 필터링하여 조회한다")
    void findAllFilter() {
        // given
        // sql-data-reservationService.sql
        final Long memberId = 100L;
        final Long themeId = 100L;
        final LocalDate startDate = LocalDate.of(2025, 5, 9);
        final LocalDate endDate = null;

        // when
        final List<ReservationResponse> reservationResponses = reservationService.findAllFilter(memberId, themeId,
                startDate, endDate);

        // then
        assertThat(reservationResponses).hasSize(2);
    }

    @Test
    @DisplayName("id를 통해 방탈출 예약을 삭제한다")
    void deleteById() {
        // given
        final ReservationResponse reservationResponse = reservationService.insert(
                new ReservationRequest(MAX_DATE_FIXTURE, memberId, timeId, themeId));
        final Long id = reservationResponse.id();
        final LoginMember loginMember = new LoginMember(1L, "mimi", "email", "USER");

        // when
        reservationService.deleteById(id, loginMember);

        // then
        final Optional<Reservation> findReservation = reservationRepository.findById(id);
        assertThat(findReservation).isEmpty();
    }

    @Test
    @DisplayName("id를 통해 예약을 삭제할 때 대상이 없다면 예외가 발생한다")
    void deleteByIdWhenNotExistReservation() {
        // given
        final Long notExistId = 999L;
        final LoginMember loginMember = new LoginMember(1L, "mimi", "email", "USER");

        // when & then
        assertThatThrownBy(() -> reservationService.deleteById(notExistId, loginMember))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("member id를 통해 내 예약 목록을 조회한다")
    void findByMemberId() {
        //given
        final Long memberId = 100L;

        //when
        final List<ReservationMineResponse> reservationMineResponses = reservationService.findByMemberId(memberId);

        //then
        assertAll(
                () -> assertThat(reservationMineResponses).hasSize(3),
                () -> assertThat(reservationMineResponses.get(0).status()).isEqualTo("예약")
        );
    }

    @Test
    @DisplayName("예약 삭제 시 대기 인원이 있으면 첫 번째 대기자가 예약으로 승격된다")
    void deleteReservationPromotesWaiting() {
        // given
        final long firstWaitingId = 100L;
        final long originalReservationId = 100L;
        final long firstWaitingMemberId = 101L;
        final int originalMemberReservationCount = 3;
        final int firstWaitingMemberReservationCount = 1;
        final LoginMember firstWaitingMember = new LoginMember(firstWaitingMemberId, "lee", "lee@test.com", "USER");

        // when
        reservationService.deleteById(originalReservationId, firstWaitingMember);

        // then
        assertAll(
                () -> assertThat(reservationRepository.findById(originalReservationId)).isNotPresent(),
                () -> assertThat(reservationRepository.findByMemberId(memberId)).hasSize(originalMemberReservationCount-1),
                () -> assertThat(waitingRepository.findById(firstWaitingId)).isNotPresent(),
                () -> assertThat(reservationRepository.findByMemberId(firstWaitingMemberId)).hasSize(firstWaitingMemberReservationCount+1)
        );
    }
}
