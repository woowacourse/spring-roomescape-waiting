package roomescape.application.reservation.query;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;

import java.time.LocalDateTime;
import java.time.LocalTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.domain.reservation.repository.WaitingRepository;

class WaitingQueryServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private WaitingRepository waitingRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private MemberRepository memberRepository;


    private WaitingQueryService waitingQueryService;

    @BeforeEach
    void setUp() {
        waitingQueryService = new WaitingQueryService(
                waitingRepository,
                memberRepository
        );
    }

    @Test
    void 대기신청을_조회할_수_있다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock).plusDays(1);
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, now.toLocalDate(), time, theme));
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();
        Waiting waiting = new Waiting(member, reservation.getDate(), reservation.getTime(), reservation.getTheme());
        Long waitingId = waitingRepository.save(waiting).getId();

        // when
        var waitingResults = waitingQueryService.findWaitingByMemberId(member.getId());

        // then
        assertAll(
                () -> assertThat(waitingResults).hasSize(1),
                () -> assertThat(waitingResults.get(0).waitingId()).isEqualTo(waitingId),
                () -> assertThat(waitingResults.get(0).reservationDate()).isEqualTo(reservation.getDate()),
                () -> assertThat(waitingResults.get(0).reservationTime()).isEqualTo(reservation.getTime().getStartAt()),
                () -> assertThat(waitingResults.get(0).themeName()).isEqualTo(reservation.getTheme().getName()),
                () -> assertThat(waitingResults.get(0).waitingCount()).isEqualTo(1)
        );
    }

    @Test
    void 전체_대기신청을_조회할_수_있다() {
        // given
        LocalDateTime now = LocalDateTime.now(clock).plusDays(1);
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        reservationRepository.save(new Reservation(member, now.toLocalDate(), time, theme));
        Reservation reservation = reservationRepository.findById(1L).orElseThrow();
        Waiting waiting = new Waiting(member, reservation.getDate(), reservation.getTime(), reservation.getTheme());
        Long waitingId = waitingRepository.save(waiting).getId();

        // when
        var waitingResults = waitingQueryService.findAll();

        // then
        assertAll(
                () -> assertThat(waitingResults).hasSize(1),
                () -> assertThat(waitingResults.get(0).waitingId()).isEqualTo(waitingId),
                () -> assertThat(waitingResults.get(0).reservationDate()).isEqualTo(reservation.getDate()),
                () -> assertThat(waitingResults.get(0).reservationTime()).isEqualTo(reservation.getTime().getStartAt()),
                () -> assertThat(waitingResults.get(0).themeName()).isEqualTo(reservation.getTheme().getName())
        );
    }
}
