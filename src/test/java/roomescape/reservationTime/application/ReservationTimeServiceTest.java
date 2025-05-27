package roomescape.reservationTime.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import roomescape.fixture.MemberFixture;
import roomescape.member.domain.Member;
import roomescape.member.domain.repository.MemberRepository;
import roomescape.member.infrastructure.MemberRepositoryAdapter;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationSpec;
import roomescape.reservation.domain.repository.ReservationRepository;
import roomescape.reservation.infrastructure.ReservationRepositoryAdapter;
import roomescape.reservationTime.application.dto.AvailableTimeRequest;
import roomescape.reservationTime.application.dto.AvailableTimeResponse;
import roomescape.reservationTime.application.dto.TimeRequest;
import roomescape.reservationTime.application.dto.TimeResponse;
import roomescape.reservationTime.domain.ReservationTime;
import roomescape.reservationTime.domain.respository.ReservationTimeRepository;
import roomescape.reservationTime.exception.TimeAlreadyExistsException;
import roomescape.reservationTime.exception.UsingTimeException;
import roomescape.reservationTime.infrastructure.ReservationTimeRepositoryAdapter;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.repository.ThemeRepository;
import roomescape.theme.infrastructure.ThemeRepositoryAdapter;

@ActiveProfiles("test")
@DataJpaTest
@Import({
        ReservationTimeService.class,
        ReservationTimeRepositoryAdapter.class,
        ReservationRepositoryAdapter.class,
        ThemeRepositoryAdapter.class,
        MemberRepositoryAdapter.class
})
class ReservationTimeServiceTest {

    @Autowired
    private ReservationTimeService reservationTimeService;

    @Autowired
    private ReservationTimeRepository timeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;

    @DisplayName("예약 시간 생성 - 성공")
    @Test
    void create() {
        // given
        // 오전 10시로 시작 시간 설정
        LocalTime startAt = LocalTime.of(10, 0);
        // 시간 생성 요청 객체 생성
        TimeRequest request = new TimeRequest(startAt);

        // when
        TimeResponse response = reservationTimeService.create(request);

        // then
        assertThat(response).isNotNull();
        assertThat(response.id()).isNotNull();
        assertThat(response.startAt()).isEqualTo(startAt);
    }

    @DisplayName("예약 시간 생성 - 이미 존재하는 시간으로 예약 시간을 생성하면 예외가 발생한다")
    @Test
    void create_timeAlreadyExists() {
        // given
        // 오전 10시로 시작 시간 설정
        LocalTime startAt = LocalTime.of(10, 0);
        // 동일한 시작 시간으로 예약 시간 객체 생성 (이미 존재하는 시간)
        ReservationTime existingTime = new ReservationTime(startAt);
        // 예약 시간 저장
        timeRepository.save(existingTime);

        // 동일한 시작 시간으로 시간 생성 요청 객체 생성
        TimeRequest request = new TimeRequest(startAt);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.create(request))
                .isInstanceOf(TimeAlreadyExistsException.class);
    }

    @DisplayName("모든 예약 시간을 조회한다")
    @Test
    void findAll() {
        // given
        // 오전 10시 예약 시간 객체 생성 및 저장
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        // 오후 12시 예약 시간 객체 생성 및 저장
        ReservationTime time2 = new ReservationTime(LocalTime.of(12, 0));
        timeRepository.save(time1);
        timeRepository.save(time2);

        // when
        List<TimeResponse> responses = reservationTimeService.findAll();

        // then
        assertThat(responses).hasSize(2);
        assertThat(responses).extracting("startAt")
                .containsExactlyInAnyOrder(LocalTime.of(10, 0), LocalTime.of(12, 0));
    }

    @DisplayName("사용 가능한 시간을 조회한다")
    @Test
    void findAvailableTimes() {
        // given
        // 오전 10시 예약 시간 객체 생성 및 저장 (이미 예약된 시간)
        ReservationTime time1 = new ReservationTime(LocalTime.of(10, 0));
        // 오후 12시 예약 시간 객체 생성 및 저장 (예약되지 않은 시간)
        ReservationTime time2 = new ReservationTime(LocalTime.of(12, 0));
        timeRepository.save(time1);
        timeRepository.save(time2);

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);
        Long themeId = theme.getId();

        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간1, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time1, theme);
        // 회원으로 예약 생성 및 저장 (시간1은 이미 예약됨)
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

        // 사용 가능한 시간 조회 요청 객체 생성 (날짜, 테마 ID)
        AvailableTimeRequest request = new AvailableTimeRequest(date, themeId);

        // when
        List<AvailableTimeResponse> responses = reservationTimeService.findAvailableTimes(request);

        // then
        assertThat(responses).hasSize(2);

        AvailableTimeResponse bookedResponse = responses.stream()
                .filter(r -> r.timeId().equals(time1.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(bookedResponse.alreadyBooked()).isTrue();

        AvailableTimeResponse availableResponse = responses.stream()
                .filter(r -> r.timeId().equals(time2.getId()))
                .findFirst()
                .orElseThrow();
        assertThat(availableResponse.alreadyBooked()).isFalse();
    }

    @DisplayName("예약 시간 삭제 - 성공")
    @Test
    void deleteById() {
        // given
        // 오전 10시 예약 시간 객체 생성
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        // 예약 시간 저장 및 저장된 객체 참조
        ReservationTime savedTime = timeRepository.save(time);
        // 저장된 예약 시간의 ID 추출
        Long timeId = savedTime.getId();

        // when
        reservationTimeService.deleteById(timeId);

        // then
        assertThat(timeRepository.findById(timeId)).isEmpty();
    }

    @DisplayName("예약 시간 삭제 - 사용 중인 예약 시간을 삭제하면 예외가 발생한다")
    @Test
    void deleteById_usingTime() {
        // given
        // 오전 10시 예약 시간 객체 생성
        ReservationTime time = new ReservationTime(LocalTime.of(10, 0));
        // 예약 시간 저장 및 저장된 객체 참조
        ReservationTime savedTime = timeRepository.save(time);
        // 저장된 예약 시간의 ID 추출
        Long timeId = savedTime.getId();

        // 테마 생성 및 저장
        Theme theme = new Theme("테마", "설명", "썸네일");
        themeRepository.save(theme);

        // 회원 생성 및 저장
        Member member = MemberFixture.createMember("에드", "ed@example.com", "password123");
        memberRepository.save(member);

        // 내일 날짜로 예약 날짜 설정
        LocalDate date = LocalDate.now().plusDays(1);
        // 예약 스펙 생성 (날짜, 시간, 테마)
        ReservationSpec spec = new ReservationSpec(new ReservationDate(date), time, theme);
        // 회원으로 예약 생성 및 저장 (시간이 사용 중인 상태)
        Reservation reservation = new Reservation(member, spec);
        reservationRepository.save(reservation);

        // when & then
        assertThatThrownBy(() -> reservationTimeService.deleteById(timeId))
                .isInstanceOf(UsingTimeException.class);
    }
}
