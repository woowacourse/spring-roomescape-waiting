package roomescape.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import roomescape.dto.request.WaitingRequest;
import roomescape.entity.Member;
import roomescape.entity.ReservationTime;
import roomescape.entity.Theme;
import roomescape.entity.Waiting;
import roomescape.entity.WaitingStatus;
import roomescape.exception.custom.DuplicatedException;
import roomescape.exception.custom.InvalidInputException;
import roomescape.repository.jpa.JpaMemberRepository;
import roomescape.repository.jpa.JpaReservationRepository;
import roomescape.repository.jpa.JpaReservationTimeRepository;
import roomescape.repository.jpa.JpaThemeRepository;
import roomescape.repository.jpa.JpaWaitingRepository;

@DataJpaTest
public class WaitingServiceTest {

    @PersistenceContext
    private EntityManager em;
    @Autowired
    private JpaReservationTimeRepository reservationTimeRepository;
    @Autowired
    private JpaMemberRepository memberRepository;
    @Autowired
    private JpaReservationRepository reservationRepository;
    @Autowired
    private JpaThemeRepository themeRepository;
    @Autowired
    private JpaWaitingRepository waitingRepository;

    private WaitingService waitingService;

    @BeforeEach
    void setUp() {
        waitingService = new WaitingService(
            waitingRepository,
            reservationRepository,
            reservationTimeRepository,
            themeRepository);
    }

    @Test
    @DisplayName("사용자는 현재 시간 이후의 예약 대기만 추가할 수 있다.")
    void addWaitingAfterNow() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(5000, 4, 28);

        em.flush();
        em.clear();

        WaitingRequest request = new WaitingRequest(date, time.getId(), theme.getId());

        assertThat(waitingService.addWaitingAfterNow(member, request)).isNotNull();
    }

    @Test
    @DisplayName("사용자는 현재 시간 이전의 예약을 추가하면 예외가 발생한다.")
    void addWaitingBeforeNow() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(2000, 4, 28);

        em.flush();
        em.clear();

        WaitingRequest request = new WaitingRequest(date, time.getId(), theme.getId());

        assertThatThrownBy(() -> waitingService.addWaitingAfterNow(member, request))
            .isInstanceOf(InvalidInputException.class)
            .hasMessageContaining("과거 예약은 불가능");
    }

    @Test
    @DisplayName("사용자가 중복된 예약 대기 존재 시 예외가 발생한다.")
    void addWaitingWithDuplicatedWaiting() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(5000, 4, 28);

        em.flush();
        em.clear();

        Waiting waiting = new Waiting(member, date, time, theme, 1L);
        waitingRepository.save(waiting);

        WaitingRequest request = new WaitingRequest(date, time.getId(), theme.getId());

        assertThatThrownBy(() -> waitingService.addWaitingAfterNow(member, request))
            .isInstanceOf(DuplicatedException.class)
            .hasMessageContaining("waiting");
    }

    @Test
    @DisplayName("관리자가 예약 승인 시 예약 대기는 예약으로 변경된다.")
    void approveWaiting() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(5000, 4, 28);

        em.flush();
        em.clear();

        Waiting waiting = new Waiting(member, date, time, theme, 1L);
        waitingRepository.save(waiting);

        waitingService.updateWaitingAndReservationStatus(waiting.getId(), WaitingStatus.APPROVED);

        assertAll(() -> {
            assertThat(waitingRepository.findById(waiting.getId()).isPresent()).isFalse();
            assertThat(reservationRepository.findByMemberId(member.getId())).hasSize(1);
        });
    }

    @Test
    @DisplayName("관리자가 예약 거절 시 예약 대기는 삭제만 된다.")
    void denyWaiting() {
        Member member = saveMember(1L);
        Theme theme = saveTheme(1L);
        ReservationTime time = saveTime(LocalTime.of(10, 0));
        LocalDate date = LocalDate.of(5000, 4, 28);

        em.flush();
        em.clear();

        Waiting waiting = new Waiting(member, date, time, theme, 1L);
        waitingRepository.save(waiting);

        waitingService.updateWaitingAndReservationStatus(waiting.getId(), WaitingStatus.DENIED);

        assertAll(() -> {
            assertThat(waitingRepository.findById(waiting.getId()).isPresent()).isFalse();
            assertThat(reservationRepository.findByMemberId(member.getId())).hasSize(0);
        });
    }

    private Member saveMember(Long tmp) {
        Member member = Member.createUser("이름" + tmp, "이메일" + tmp, "비밀번호" + tmp);
        memberRepository.save(member);

        return member;
    }

    private Theme saveTheme(Long tmp) {
        Theme theme = new Theme("이름" + tmp, "설명" + tmp, "썸네일" + tmp);
        themeRepository.save(theme);

        return theme;
    }

    private ReservationTime saveTime(LocalTime reservationTime) {
        ReservationTime time = new ReservationTime(reservationTime);
        reservationTimeRepository.save(time);

        return time;
    }
}
