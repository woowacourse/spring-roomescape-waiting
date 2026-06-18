package roomescape.domain.reservation;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.time.LocalDate;
import java.time.LocalTime;
import org.junit.jupiter.api.Test;
import roomescape.domain.member.Member;
import roomescape.domain.reservationdate.ReservationDate;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.support.exception.MemberErrorCode;
import roomescape.support.exception.RoomescapeException;

class ReservationTest {

    @Test
    void id가_없는_예약을_생성한다() {
        Member member = Member.of(1L, "보예");
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        Reservation reservation = Reservation.createWithoutId(member, date, time, theme);

        assertSoftly(softly -> {
                softly.assertThat(reservation.getId()).isNull();
                softly.assertThat(reservation.getMember()).isEqualTo(member);
                softly.assertThat(reservation.getDate()).isEqualTo(date);
                softly.assertThat(reservation.getTime()).isEqualTo(time);
                softly.assertThat(reservation.getTheme()).isEqualTo(theme);
            }
        );
    }

    @Test
    void id를_부여한_예약을_생성한다() {
        Member member = Member.of(1L, "보예");
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");
        Reservation reservation = Reservation.createWithoutId(member, date, time, theme);

        Reservation reservationWithId = Reservation.of(1L, member, date, time, theme);

        assertSoftly(softly -> {
                assertThat(reservationWithId.getId()).isEqualTo(1L);
                assertThat(reservationWithId.getMember()).isEqualTo(member);
                assertThat(reservationWithId.getDate()).isEqualTo(date);
                assertThat(reservationWithId.getTime()).isEqualTo(time);
                assertThat(reservationWithId.getTheme()).isEqualTo(theme);
            }
        );
    }

    @Test
    void DB에서_조회한_예약을_생성한다() {
        long id = 1L;
        Member member = Member.of(2L, "보예");
        ReservationDate date = ReservationDate.of(2L, LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        Reservation reservation = Reservation.of(id, member, date, time, theme);

        assertSoftly(softly -> {
                assertThat(reservation.getId()).isEqualTo(id);
                assertThat(reservation.getMember()).isEqualTo(member);
                assertThat(reservation.getDate()).isEqualTo(date);
                assertThat(reservation.getTime()).isEqualTo(time);
                assertThat(reservation.getTheme()).isEqualTo(theme);
            }
        );
    }

    @Test
    void 멤버가_null이면_예외가_발생한다() {
        Member member = null;
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        assertThatThrownBy(() -> Reservation.createWithoutId(member, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage(MemberErrorCode.INVALID_MEMBER.getMessage());
    }

    @Test
    void 날짜가_null이면_예외가_발생한다() {
        Member member = Member.of(1L, "보예");
        ReservationDate date = null;
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        assertThatThrownBy(() -> Reservation.createWithoutId(member, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("예약 날짜 식별자 혹은 데이터가 누락되었습니다.");
    }

    @Test
    void 예약_시간이_null이면_예외가_발생한다() {
        Member member = Member.of(1L, "보예");
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = null;
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");

        assertThatThrownBy(() -> Reservation.createWithoutId(member, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("예약 시간 식별자 정보가 누락되었습니다.");
    }

    @Test
    void 테마가_null이면_예외가_발생한다() {
        Member member = Member.of(1L, "보예");
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2023, 8, 5));
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        Theme theme = null;

        assertThatThrownBy(() -> Reservation.createWithoutId(member, date, time, theme))
            .isInstanceOf(RoomescapeException.class)
            .hasMessage("테마 엔티티 식별자 정보가 누락되었습니다.");
    }

    @Test
    void 날짜_시간을_업데이트_할_수_있다() {
        Member member = Member.of(1L, "이산");
        ReservationTime time = ReservationTime.createWithoutId(LocalTime.of(15, 40));
        ReservationTime updateTime = ReservationTime.createWithoutId(LocalTime.of(10, 0));
        ReservationDate date = ReservationDate.createWithoutId(LocalDate.of(2026, 8, 5));
        ReservationDate updateDate = ReservationDate.createWithoutId(LocalDate.of(2026, 8, 15));
        Theme theme = Theme.of(1L, "공포", "무서운 테마", "theme-url");
        Reservation reservation = Reservation.createWithoutId(member, date, time, theme);

        reservation.update(updateDate, updateTime);

        assertThat(reservation.getDate()).isEqualTo(updateDate);
        assertThat(reservation.getTime()).isEqualTo(updateTime);
    }
}
