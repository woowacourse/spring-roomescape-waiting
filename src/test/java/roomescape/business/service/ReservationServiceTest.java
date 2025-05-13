package roomescape.business.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.jdbc.Sql;
import roomescape.business.domain.Reservation;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidDateAndTimeException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.presentation.dto.ReservationResponse;

@DataJpaTest
@Sql("classpath:data-reservationService.sql")
public class ReservationServiceTest {

    private static final LocalDate MAX_DATE_FIXTURE = LocalDate.of(9999, 12, 31);

    private final ReservationService reservationService;
    private final ReservationRepository reservationRepository;

    // data-reservationService.sql
    private final Long memberId = 100L;
    private final Long timeId = 100L;
    private final Long themeId = 100L;

    @Autowired
    public ReservationServiceTest(final MemberRepository memberRepository,
                                  final ReservationTimeRepository reservationTimeRepository,
                                  final ThemeRepository themeRepository,
                                  final ReservationRepository reservationRepository) {

        this.reservationService = new ReservationService(reservationRepository,
                                                         memberRepository,
                reservationTimeRepository,
                                                         themeRepository);
        this.reservationRepository = reservationRepository;
    }

    @Test
    @DisplayName("방탈출 예약 요청 객체로 방탈출 예약을 저장한다")
    void insert() {
        // when
        final ReservationResponse reservationResponse = reservationService.insert(MAX_DATE_FIXTURE, memberId, timeId,
                themeId);

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
        assertThatThrownBy(() -> reservationService.insert(MAX_DATE_FIXTURE, notExistMemberId, timeId, themeId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 방탈출 예약 시간으로 예약하면 예외가 발생한다")
    void insertWhenNotExistReservationTime() {
        // given
        final Long notExistTimeId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationService.insert(MAX_DATE_FIXTURE, memberId, notExistTimeId, themeId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("존재하지 않는 테마로 예약하면 예외가 발생한다")
    void insertWhenNotExistTheme() {
        // given
        final Long notExistThemeId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationService.insert(MAX_DATE_FIXTURE, memberId, timeId, notExistThemeId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약하려는 방탈출 예약과 동일한 날짜, 시간, 테마가 이미 존재한다면 예외가 발생한다")
    void insertWhenDuplicateDateAndTimeAndTheme() {
        // given
        reservationService.insert(MAX_DATE_FIXTURE, memberId, timeId, themeId);

        // when & then
        assertThatThrownBy(() -> reservationService.insert(MAX_DATE_FIXTURE, memberId, timeId, themeId))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("예약 시간이 현재를 기준으로 과거라면 예외가 발생한다")
    void insertWhenDateAndTimeIsPast() {
        // TODO: 현재 시간을 비교하는 유틸을 인터페이스로 구현하여 테스트 완성도 높이기
        // given
        final LocalDate pastDate = LocalDate.MIN;

        // when & then
        assertThatThrownBy(() -> reservationService.insert(pastDate, memberId, timeId, themeId))
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
        final ReservationResponse reservationResponse = reservationService.insert(MAX_DATE_FIXTURE, memberId, timeId,
                themeId);
        final Long id = reservationResponse.id();

        // when
        reservationService.deleteById(id);

        // then
        final Optional<Reservation> findReservation = reservationRepository.findById(id);
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
}
