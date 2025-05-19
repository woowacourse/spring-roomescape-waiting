package roomescape.application.reservation.command;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.application.reservation.command.dto.CreateReservationCommand;
import roomescape.domain.member.Member;
import roomescape.domain.member.repository.MemberRepository;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.repository.ReservationRepository;
import roomescape.domain.reservation.repository.ReservationTimeRepository;
import roomescape.domain.reservation.repository.ThemeRepository;
import roomescape.infrastructure.error.exception.MemberException;
import roomescape.infrastructure.error.exception.ReservationException;
import roomescape.infrastructure.error.exception.ReservationTimeException;
import roomescape.infrastructure.error.exception.ThemeException;

@Service
@Transactional
public class CreateReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final Clock clock;

    public CreateReservationService(ReservationTimeRepository reservationTimeRepository,
                                    ReservationRepository reservationRepository,
                                    ThemeRepository themeRepository,
                                    MemberRepository memberRepository,
                                    Clock clock) {
        this.reservationTimeRepository = reservationTimeRepository;
        this.reservationRepository = reservationRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.clock = clock;
    }

    public Long create(CreateReservationCommand createReservationCommand) {
        Member member = memberRepository.findById(createReservationCommand.memberId())
                .orElseThrow(() -> new MemberException("존재하지 않는 회원입니다."));
        ReservationTime reservationTime = reservationTimeRepository.findById(createReservationCommand.timeId())
                .orElseThrow(() -> new ReservationTimeException("존재하지 않는 예약 시간입니다."));
        Theme theme = themeRepository.findById(createReservationCommand.themeId())
                .orElseThrow(() -> new ThemeException("존재하지 않는 테마입니다."));
        if (isAlreadyReservedAt(createReservationCommand.date(), reservationTime, theme)) {
            throw new ReservationException("날짜와 시간이 중복된 예약이 존재합니다.");
        }
        Reservation reservation = new Reservation(
                member,
                createReservationCommand.date(),
                reservationTime,
                theme
        );
        reservation.validateReservable(LocalDateTime.now(clock));
        return reservationRepository.save(reservation).getId();
    }

    private boolean isAlreadyReservedAt(LocalDate date, ReservationTime reservationTime, Theme theme) {
        return reservationRepository.existsByDateAndTimeIdAndThemeId(date, reservationTime.getId(), theme.getId());
    }
}
