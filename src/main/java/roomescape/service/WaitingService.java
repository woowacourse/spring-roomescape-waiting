package roomescape.service;

import static roomescape.domain.reservation.ReservationStatus.WAITING;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Date;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.system.exception.RoomescapeException;

@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final MemberRepository memberRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;

    public WaitingService(ReservationRepository reservationRepository, MemberRepository memberRepository, ReservationTimeRepository reservationTimeRepository, ThemeRepository themeRepository) {
        this.reservationRepository = reservationRepository;
        this.memberRepository = memberRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
    }

    public Reservation saveWaiting(Long memberId, String date, Long timeId, Long themeId) {
        Member member = findMember(memberId);
        ReservationTime time = findTime(timeId);
        Theme theme = findTheme(themeId);

        if (reservationRepository.existsByMemberIdAndTimeIdAndThemeIdAndDate(memberId, timeId, themeId, new Date(date))) {
            throw new RoomescapeException("동일한 멤버가 다수의 예약을 생성할 수 없습니다.");
        }

        Reservation reservation = new Reservation(member, date, time, theme, WAITING);

        return reservationRepository.save(reservation);
    }

    private Member findMember(Long memberId) {
        return memberRepository.findById(memberId)
            .orElseThrow(() -> new RoomescapeException("입력한 사용자 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    private ReservationTime findTime(Long timeId) {
        return reservationTimeRepository.findById(timeId)
            .orElseThrow(() -> new RoomescapeException("입력한 시간 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    private Theme findTheme(Long themeId) {
        return themeRepository.findById(themeId)
            .orElseThrow(() -> new RoomescapeException("입력한 테마 ID에 해당하는 데이터가 존재하지 않습니다."));
    }

    public List<Reservation> findAllWaitingReservations() {
        return reservationRepository.findAllByStatus(WAITING);
    }

    public void deleteWaiting(Member member, Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException("주어진 id에 해당하는 예약대기가 존재하지 않습니다."));
        reservation.validateNotWaiting();

        if (member.isNotAdmin()) {
            reservation.validateNotMyWaiting(member.getId());
        }

        reservationRepository.deleteById(id);
    }
}
