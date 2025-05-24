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
import roomescape.exception.NotFoundException;
import roomescape.persistence.repository.MemberRepository;
import roomescape.persistence.repository.ReservationRepository;
import roomescape.persistence.repository.ReservationTimeRepository;
import roomescape.persistence.repository.ThemeRepository;
import roomescape.presentation.dto.ReservationAvailableTimeResponse;
import roomescape.presentation.dto.ReservationTimeRequest;
import roomescape.presentation.dto.ReservationTimeResponse;

@DataJpaTest
class ReservationTimeServiceTest {

    private final ReservationTimeService reservationTimeService;
    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;

    @Autowired
    public ReservationTimeServiceTest(
            final ReservationRepository reservationRepository,
            final ReservationTimeRepository reservationTimeRepository,
            final MemberRepository memberRepository,
            final ThemeRepository themeRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeService = new ReservationTimeService(reservationTimeRepository, reservationRepository);
    }

    @Test
    @DisplayName("방탈출 예약 시간 요청 객체로 방탈출 예약 시간을 저장한다")
    void insert() {
        // given
        final LocalTime startAt = LocalTime.of(10, 10);
        final ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(startAt);

        // when
        final ReservationTimeResponse reservationTimeResponse = reservationTimeService.insert(reservationTimeRequest);

        // then
        assertThat(reservationTimeResponse.startAt()).isEqualTo(startAt);
    }

    @Test
    @DisplayName("저장하려는 방탈출 예약 시간과 동일한 방탈출 예약 시간이 이미 존재한다면 예외가 발생한다")
    void insertWhenStartAtIsDuplicate() {
        // given
        final LocalTime startAt = LocalTime.of(10, 10);
        final ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(startAt);
        reservationTimeService.insert(reservationTimeRequest);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.insert(reservationTimeRequest))
                .isInstanceOf(DuplicateException.class);
    }

    @Test
    @DisplayName("모든 방탈출 예약 시간을 조회한다")
    void findAll() {
        // given
        reservationTimeRepository.saveAll(List.of(
                new ReservationTime(LocalTime.of(10, 0)),
                new ReservationTime(LocalTime.of(12, 0))
        ));

        // when
        final List<ReservationTimeResponse> reservationTimeResponses = reservationTimeService.findAll();

        // then
        assertThat(reservationTimeResponses).hasSize(2);
    }

    @Test
    @DisplayName("id를 통해 방탈출 예약 시간을 조회한다")
    void findById() {
        // given
        final LocalTime startAt = LocalTime.of(10, 10);
        final ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(startAt);
        final ReservationTimeResponse reservationTimeResponse = reservationTimeService.insert(reservationTimeRequest);
        final Long id = reservationTimeResponse.id();

        // when
        final ReservationTimeResponse findReservationTimeResponse = reservationTimeService.findById(id);

        // then
        assertAll(
                () -> assertThat(findReservationTimeResponse.id()).isEqualTo(id),
                () -> assertThat(findReservationTimeResponse.startAt()).isEqualTo(startAt)
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
        final ReservationTimeRequest reservationTimeRequest = new ReservationTimeRequest(startAt);
        final ReservationTimeResponse reservationTimeResponse = reservationTimeService.insert(reservationTimeRequest);
        final Long id = reservationTimeResponse.id();

        // when
        reservationTimeService.deleteById(id);

        // then
        final Optional<ReservationTime> findTime = reservationTimeRepository.findById(id);
        assertThat(findTime).isEmpty();
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
        // 2개의 방탈출 예약 시간, 1개의 테마, 1개의 예약이 주어진다.
        final Theme theme = new Theme("테마", "소개", "썸네일");
        themeRepository.save(theme);

        final Member member = new Member("후유", "ADMIN", "fuyu@test.com", "pass");
        memberRepository.save(member);

        final ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        reservationTimeRepository.save(time1);
        final ReservationTime time2 = new ReservationTime(LocalTime.of(12, 0));
        reservationTimeRepository.save(time2);

        final LocalDate date = LocalDate.parse("2025-05-10");
        final Reservation reservation = new Reservation(
                date,
                time1,
                theme
        );
        reservationRepository.save(reservation);

        // when
        final List<ReservationAvailableTimeResponse> availableTimeResponses = reservationTimeService.findAvailableTimes(
                date,
                theme.getId()
        );

        // then
        final ReservationAvailableTimeResponse alreadyBookedResponse = availableTimeResponses.stream()
                .filter(response -> response.reservationTime().getId().equals(time1.getId()))
                .findFirst().get();
        final ReservationAvailableTimeResponse notAlreadyBookedResponse = availableTimeResponses.stream()
                .filter(response -> response.reservationTime().getId().equals(time2.getId()))
                .findFirst().get();
        assertAll(
                () -> assertThat(availableTimeResponses).hasSize(2),
                () -> assertThat(alreadyBookedResponse.alreadyBooked()).isTrue(),
                () -> assertThat(notAlreadyBookedResponse.alreadyBooked()).isFalse()
        );
    }
}
