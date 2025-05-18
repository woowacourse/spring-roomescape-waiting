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
import org.springframework.test.context.jdbc.Sql;
import roomescape.business.domain.ReservationTime;
import roomescape.exception.DuplicateException;
import roomescape.exception.NotFoundException;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.presentation.dto.PlayTimeRequest;
import roomescape.presentation.dto.PlayTimeResponse;
import roomescape.presentation.dto.ReservationAvailableTimeResponse;

@DataJpaTest
@Sql("classpath:data-playTimeService.sql")
class ReservationTimeServiceTest {

    private final ReservationTimeService reservationTimeService;
    private final ReservationTimeRepository reservationTimeRepository;

    @Autowired
    public ReservationTimeServiceTest(final ReservationTimeRepository reservationTimeRepository, final ReservationRepository reservationRepository) {
        this.reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);
        this.reservationTimeRepository = reservationTimeRepository;
    }

    @Test
    @DisplayName("방탈출 예약 시간 요청 객체로 방탈출 예약 시간을 저장한다")
    void insert() {
        // given
        final LocalTime startAt = LocalTime.of(10, 10);
        final PlayTimeRequest playTimeRequest = new PlayTimeRequest(startAt);

        // when
        final PlayTimeResponse playTimeResponse = reservationTimeService.insert(playTimeRequest);

        // then
        assertThat(playTimeResponse.startAt()).isEqualTo(startAt);
    }

    @Test
    @DisplayName("저장하려는 방탈출 예약 시간과 동일한 방탈출 예약 시간이 이미 존재한다면 예외가 발생한다")
    void insertWhenStartAtIsDuplicate() {
        // given
        final LocalTime startAt = LocalTime.of(10, 10);
        final PlayTimeRequest playTimeRequest = new PlayTimeRequest(startAt);
        reservationTimeService.insert(playTimeRequest);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.insert(playTimeRequest))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("모든 방탈출 예약 시간을 조회한다")
    void findAll() {
        // given
        // data-playTimeService.sql
        // 2개의 방탈출 예약 시간이 주어진다.

        // when
        final List<PlayTimeResponse> playTimeResponses = reservationTimeService.findAll();

        // then
        assertThat(playTimeResponses).hasSize(2);
    }

    @Test
    @DisplayName("id를 통해 방탈출 예약 시간을 조회한다")
    void findByIdById() {
        // given
        final LocalTime startAt = LocalTime.of(10, 10);
        final PlayTimeRequest playTimeRequest = new PlayTimeRequest(startAt);
        final PlayTimeResponse playTimeResponse = reservationTimeService.insert(playTimeRequest);
        final Long id = playTimeResponse.id();

        // when
        final PlayTimeResponse findPlayTimeResponse = reservationTimeService.findById(id);

        // then
        assertAll(
                () -> assertThat(findPlayTimeResponse.id()).isEqualTo(id),
                () -> assertThat(findPlayTimeResponse.startAt()).isEqualTo(startAt)
        );
    }

    @Test
    @DisplayName("id를 통해 방탈출 예약 시간을 조회할 때 대상이 없다면 예외가 발생한다")
    void findByIdWhenNotExists() {
        // given
        final Long notExistsId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationTimeService.findById(notExistsId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("id를 통해 방탈출 예약 시간을 삭제한다")
    void deleteById() {
        // given
        final LocalTime startAt = LocalTime.of(14, 0);
        final PlayTimeRequest playTimeRequest = new PlayTimeRequest(startAt);
        final PlayTimeResponse playTimeResponse = reservationTimeService.insert(playTimeRequest);
        final Long id = playTimeResponse.id();

        // when
        reservationTimeService.deleteById(id);

        // then
        final Optional<ReservationTime> findPlayTime = reservationTimeRepository.findById(id);
        assertThat(findPlayTime).isEmpty();
    }

    @Test
    @DisplayName("id를 통해 방탈출 예약 시간을 삭제할 때 대상이 없다면 예외가 발생한다")
    void deleteByIdWhenNotExists() {
        // given
        final Long notExistsId = 999L;

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(notExistsId))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    @DisplayName("예약 가능 시간 목록을 조회한다")
    void findAvailableTimes() {
        // given
        // data-playTimeService.sql
        // 2개의 방탈출 예약 시간, 1개의 테마, 1개의 예약이 주어진다.

        // when
        final List<ReservationAvailableTimeResponse> availableTimeResponses = reservationTimeService.findAvailableTimes(
                LocalDate.parse("2025-05-10"),
                100L
        );

        // then
        final ReservationAvailableTimeResponse notAvailableTimeResponse = availableTimeResponses.stream()
                        .filter(response -> response.reservationTimeResponse().id() == 100L)
                .findFirst().get();
        final ReservationAvailableTimeResponse availableTimeResponse = availableTimeResponses.stream()
                        .filter(response -> response.reservationTimeResponse().id() == 101L)
                .findFirst().get();
        assertAll(
                () -> assertThat(availableTimeResponses).hasSize(2),
                ()-> assertThat(notAvailableTimeResponse.alreadyBooked()).isTrue(),
                () -> assertThat(availableTimeResponse.alreadyBooked()).isFalse()
        );
    }
}
