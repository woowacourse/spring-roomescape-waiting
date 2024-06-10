package roomescape.service;

import static roomescape.domain.reservation.ReservationStatus.RESERVED;

import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.member.MemberRepository;
import roomescape.domain.reservation.Date;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.system.exception.RoomescapeException;

@Service
public class ReservationService {

    public static final int EXCLUDE_CURRENT_RESERVATION = 1;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationTimeRepository reservationTimeRepository,
        ThemeRepository themeRepository,
        MemberRepository memberRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
    }

    public Reservation save(Long memberId, String date, Long timeId, Long themeId) {
        Member member = findMember(memberId);
        ReservationTime time = findTime(timeId);
        Theme theme = findTheme(themeId);
        Reservation reservation = new Reservation(member, date, time, theme, RESERVED);

        List<Reservation> reservations = reservationRepository.findAllByDateAndTimeIdAndThemeId(
            new Date(date), timeId, themeId);
        reservation.validateDuplication(reservations);

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

    public void delete(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException("ID에 해당하는 예약이 존재하지 않습니다."));

        List<Reservation> reservations = reservationRepository.findAllByDateAndTimeIdAndThemeId(
            reservation.getDate(),
            reservation.getTime().getId(),
            reservation.getTheme().getId());

        reservations.stream()
            .sorted(Comparator.comparingLong(Reservation::getId))
            .skip(EXCLUDE_CURRENT_RESERVATION)
            .findFirst()
            .ifPresentOrElse(
                waiting -> {
                    waiting.setStatus(RESERVED);
                    reservationRepository.save(waiting);
                    reservationRepository.deleteById(id);
                },
                () -> reservationRepository.deleteById(id)
            );
    }
}
