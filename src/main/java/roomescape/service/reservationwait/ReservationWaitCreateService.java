package roomescape.service.reservationwait;

import static roomescape.domain.reservationwait.ReservationWaitStatus.WAITING;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationwait.ReservationWait;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.exception.InvalidRequestException;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ReservationWaitRepository;
import roomescape.repository.ThemeRepository;
import roomescape.service.dto.request.ReservationWaitSaveRequest;

@Service
public class ReservationWaitCreateService {

    private final ReservationWaitRepository reservationWaitRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final ReservationRepository reservationRepository;

    public ReservationWaitCreateService(ReservationWaitRepository reservationWaitRepository,
                                        ReservationTimeRepository reservationTimeRepository,
                                        ThemeRepository themeRepository, ReservationRepository reservationRepository) {
        this.reservationWaitRepository = reservationWaitRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public ReservationWait create(ReservationWaitSaveRequest request, Member member) {
        ReservationTime reservationTime = getReservationTime(request.time());
        ReservationWait reservationWait = request.toEntity(
                reservationTime,
                getTheme(request.theme()),
                member);
        validateAlreadyReserved(request.date(), reservationTime.getId(), request.time(), member.getId());
        validateAlreadyWait(request.date(), reservationTime.getId(), request.time(), member.getId());
        validateDateIsFuture(request.date(), reservationTime);
        return reservationWaitRepository.save(reservationWait);
    }

    private ReservationTime getReservationTime(long reservationId) {
        return reservationTimeRepository.findById(reservationId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 예약 시간 입니다."));
    }

    private Theme getTheme(long themeId) {
        return themeRepository.findById(themeId)
                .orElseThrow(() -> new InvalidRequestException("존재하지 않는 테마 입니다."));
    }

    private void validateAlreadyReserved(LocalDate date, long timeId, long themeId, long memberId) {
        if (reservationRepository.existsByDateAndTimeIdAndThemeIdAndMemberId(date, timeId, themeId, memberId)) {
            throw new InvalidRequestException("이미 예약 중입니다.");
        }
    }

    private void validateAlreadyWait(LocalDate date, long timeId, long themeId, long memberId) {
        if (reservationWaitRepository.existsByDateAndTimeIdAndThemeIdAndMemberIdAndStatus(date, timeId, themeId,
                memberId, WAITING)) {
            throw new InvalidRequestException("이미 예약 대기 중입니다.");
        }
    }

    private void validateDateIsFuture(LocalDate date, ReservationTime reservationTime) {
        LocalDateTime localDateTime = LocalDateTime.of(date, reservationTime.getStartAt());
        if (localDateTime.isBefore(LocalDateTime.now())) {
            throw new InvalidRequestException("지나간 날짜와 시간에 대한 예약 대기 생성은 불가능합니다.");
        }
    }
}
