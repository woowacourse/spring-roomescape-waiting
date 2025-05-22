package roomescape.service.waiting;

import org.springframework.stereotype.Service;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.member.Member;
import roomescape.domain.waiting.Waiting;
import roomescape.dto.waiting.WaitingResponseDto;
import roomescape.exception.DuplicateContentException;
import roomescape.exception.NotFoundException;
import roomescape.repository.*;
import roomescape.service.dto.WaitingCreateDto;

import java.time.LocalDate;

@Service
public class WaitingCommandService {

    private final JpaWaitingRepository waitingRepository;
    private final JpaReservationTimeRepository reservationTimeRepository;
    private final JpaThemeRepository themeRepository;
    private final JpaMemberRepository memberRepository;
    private final JpaReservationRepository reservationRepository;

    public WaitingCommandService(JpaWaitingRepository waitingRepository,
                                 JpaReservationTimeRepository reservationTimeRepository,
                                 JpaThemeRepository themeRepository,
                                 JpaMemberRepository memberRepository,
                                 JpaReservationRepository reservationRepository) {
        this.waitingRepository = waitingRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.memberRepository = memberRepository;
        this.reservationRepository = reservationRepository;
    }

    public WaitingResponseDto registerWaiting(WaitingCreateDto dto) {
        validateDuplicate(dto.localDate(), dto.timeId(), dto.themeId(), dto.memberId());

        ReservationTime reservationTime = reservationTimeRepository.findById(dto.timeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 예약 시간을 찾을 수 없습니다. id : " + dto.timeId()));

        Theme theme = themeRepository.findById(dto.themeId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 테마를 찾을 수 없습니다. id : " + dto.themeId()));

        Member member = memberRepository.findById(dto.memberId())
                .orElseThrow(() -> new NotFoundException("[ERROR] 유저를 찾을 수 없습니다. id : " + dto.memberId()));

        Waiting waiting = Waiting.createWithoutId(member, dto.localDate(), reservationTime, theme);
        Waiting saved = waitingRepository.save(waiting);
        WaitingResponseDto waitingResponseDto = WaitingResponseDto.of(saved, reservationTime, theme);
        return waitingResponseDto;
    }

    private void validateDuplicate(LocalDate date, long timeId, long themeId, long memberId) {
        boolean reservationExist = reservationRepository.existsFor(date, timeId, themeId, memberId);
        if(reservationExist) {
            throw new DuplicateContentException("[ERROR] 이미 예약이 존재합니다.");
        }

        boolean exists = waitingRepository.existsFor(date, timeId, themeId, memberId);
        if (exists) {
            throw new DuplicateContentException("[ERROR] 이미 예약 대기가 존재합니다.");
        }
    }

    public void cancelWaiting(long id) {
        if(!waitingRepository.existsById(id)){
            throw new NotFoundException("[ERROR] 등록된 예약번호만 삭제할 수 있습니다. 입력된 번호는 " + id + "입니다.");
        }
        waitingRepository.deleteById(id);
    }
}
