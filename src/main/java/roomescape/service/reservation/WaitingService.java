package roomescape.service.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.ReservationTime;
import roomescape.domain.reservation.Theme;
import roomescape.domain.reservation.Waiting;
import roomescape.global.handler.exception.NotFoundException;
import roomescape.global.handler.exception.ValidationException;
import roomescape.repository.MemberRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.reservation.dto.request.WaitingRequest;
import roomescape.service.reservation.dto.response.WaitingResponse;

@Service
public class WaitingService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final MemberRepository memberRepository;
    private final WaitingRepository waitingRepository;

    public WaitingService(ReservationRepository reservationRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          ThemeRepository themeRepository,
                          MemberRepository memberRepository,
                          WaitingRepository waitingRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.waitingRepository = waitingRepository;
    }

    public WaitingResponse createWaiting(WaitingRequest waitingRequest, Long memberId) {
        Member member = getMember(memberId);
        ReservationTime reservationTime = getReservationTime(waitingRequest.timeId());
        Theme theme = getTheme(waitingRequest.themeId());

        validateIsWaitingInThePast(waitingRequest.date(), reservationTime);
        validateMemberWaitingAlreadyExist(waitingRequest.date(), theme, member);
        validateMemberReservationAlreadyExist(waitingRequest.date(), theme, member);

        Waiting waiting = waitingRequest.toEntity(member, reservationTime, theme);
        Waiting savedReservation = waitingRepository.save(waiting);
        return WaitingResponse.from(savedReservation);
    }

    private void validateMemberWaitingAlreadyExist(LocalDate date, Theme theme, Member member) {
        if (waitingRepository.existsBySchedule_DateAndSchedule_ThemeAndMember(date, theme, member)) {
            throw new ValidationException("테마당 하나의 날짜에 하나의 예약대기를 생성할 수 있습니다.");
        }
    }

    private void validateMemberReservationAlreadyExist(LocalDate date, Theme theme,
                                                       Member member) {
        if (reservationRepository.existsBySchedule_DateAndSchedule_ThemeAndMember(date, theme, member)) {
            throw new ValidationException(String.format("해당 (날짜: %s / 테마: %s)에 회원님에 대한 예약이 이미 존재합니다.",
                    date.toString(), theme.getName()));        }
    }

    private void validateIsWaitingInThePast(LocalDate date, ReservationTime time) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (LocalDateTime.now().isAfter(reservationDateTime)) {
            throw new ValidationException("이미 지나간 시점을 예약할 수 없습니다.");
        }
    }

    public List<WaitingResponse> findAllWaitings() {
        List<Waiting> waitings = waitingRepository.findAll();
        return waitings.stream()
                .map(WaitingResponse::from)
                .toList();
    }

    public void deleteWaiting(Long id) {
        waitingRepository.deleteById(id);
    }

    private Theme getTheme(Long id) {
        return themeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("id값: %d에 대한 테마가 존재하지 않습니다.", id)));
    }

    private ReservationTime getReservationTime(Long id) {
        return reservationTimeRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("id값: %d에 대한 예약시간이 존재하지 않습니다.", id)));
    }

    private Member getMember(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("id값: %d에 대한 멤버가 존재하지 않습니다.", id)));
    }
}
