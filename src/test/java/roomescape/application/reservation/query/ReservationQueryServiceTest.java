package roomescape.application.reservation.query;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import roomescape.application.AbstractServiceIntegrationTest;
import roomescape.application.member.query.dto.MemberResult;
import roomescape.application.reservation.query.dto.ReservationResult;
import roomescape.application.reservation.query.dto.ReservationSearchCondition;
import roomescape.application.reservation.query.dto.ReservationTimeResult;
import roomescape.application.reservation.query.dto.ReservationWithStatusResult;
import roomescape.application.reservation.query.dto.ThemeResult;
import roomescape.domain.member.Email;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRole;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.repository.ThemeRepository;

class ReservationQueryServiceTest extends AbstractServiceIntegrationTest {

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private ReservationTimeRepository reservationTimeRepository;

    @Autowired
    private MemberRepository memberRepository;

    private ReservationQueryService reservationQueryService;

    @BeforeEach
    void setUp() {
        reservationQueryService = new ReservationQueryService(reservationRepository);
    }

    @Test
    void 전체_예약을_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));
        Reservation reservation1 = reservationRepository.save(
                new Reservation(member, LocalDate.now(clock), time1, theme));
        Reservation reservation2 = reservationRepository.save(
                new Reservation(member, LocalDate.now(clock), time2, theme));

        // when
        List<ReservationResult> reservationResults = reservationQueryService.findAll();

        // then
        assertThat(reservationResults)
                .isEqualTo(List.of(
                                new ReservationResult(
                                        reservation1.getId(),
                                        new MemberResult(1L, "벨로"),
                                        LocalDate.now(clock),
                                        new ReservationTimeResult(1L, LocalTime.of(13, 0)),
                                        new ThemeResult(1L, "테마", "설명", "이미지")
                                ),
                                new ReservationResult(
                                        reservation2.getId(),
                                        new MemberResult(1L, "벨로"),
                                        LocalDate.now(clock),
                                        new ReservationTimeResult(2L, LocalTime.of(14, 0)),
                                        new ThemeResult(1L, "테마", "설명", "이미지")
                                )
                        )
                );
    }

    @Test
    void 검색_조건에_맞는_예약을_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));
        Reservation reservation1 = reservationRepository.save(
                new Reservation(member, LocalDate.now(clock), time1, theme)
        );
        Reservation reservation2 = reservationRepository.save(
                new Reservation(member, LocalDate.now(clock).plusDays(1), time2, theme)
        );
        ReservationSearchCondition reservationSearchCondition = new ReservationSearchCondition(
                theme.getId(),
                member.getId(),
                LocalDate.now(clock),
                LocalDate.now(clock)
        );

        // when
        List<ReservationResult> reservationResults = reservationQueryService.findReservationsBy(
                reservationSearchCondition
        );

        // then
        assertThat(reservationResults)
                .isEqualTo(List.of(
                                new ReservationResult(
                                        reservation1.getId(),
                                        new MemberResult(1L, "벨로"),
                                        LocalDate.now(clock),
                                        new ReservationTimeResult(1L, LocalTime.of(13, 0)),
                                        new ThemeResult(1L, "테마", "설명", "이미지")
                                )
                        )
                );
    }

    @Test
    void 사용자의_예약을_모두_조회할_수_있다() {
        // given
        Member member = memberRepository.save(new Member("벨로", new Email("test@email.com"), "pw", MemberRole.NORMAL));
        Theme theme = themeRepository.save(new Theme("테마", "설명", "이미지"));
        ReservationTime time1 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(13, 0)));
        ReservationTime time2 = reservationTimeRepository.save(new ReservationTime(LocalTime.of(14, 0)));
        Reservation reservation1 = reservationRepository.save(
                new Reservation(member, LocalDate.now(clock), time1, theme)
        );
        Reservation reservation2 = reservationRepository.save(
                new Reservation(member, LocalDate.now(clock).plusDays(1), time2, theme)
        );

        // when
        List<ReservationWithStatusResult> reservationsWithStatus = reservationQueryService.findReservationsWithStatus(
                member.getId()
        );

        // then
        assertThat(reservationsWithStatus)
                .isEqualTo(List.of(
                        new ReservationWithStatusResult(
                                reservation1.getId(),
                                "테마",
                                LocalDate.now(clock),
                                LocalTime.of(13, 0),
                                ReservationStatus.RESERVE
                        ),
                        new ReservationWithStatusResult(
                                reservation2.getId(),
                                "테마",
                                LocalDate.now(clock).plusDays(1),
                                LocalTime.of(14, 0),
                                ReservationStatus.RESERVE
                        )
                ));
    }
}
