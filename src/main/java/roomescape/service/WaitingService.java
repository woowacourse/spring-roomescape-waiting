package roomescape.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.*;
import roomescape.exception.NotFoundMemberException;
import roomescape.exception.NotFoundReservationTimeException;
import roomescape.exception.NotFoundThemeException;
import roomescape.service.param.CreateWaitingParam;
import roomescape.service.result.WaitingResult;

import java.time.LocalDate;

@Service
@Transactional
public class WaitingService {

    private final WaitingRepository waitingRepository;
    private final MemberRepository memberRepository;
    private final ThemeRepository themeRepository;
    private final ReservationTimeRepository reservationTimeRepository;

    public WaitingService(MemberRepository memberRepository,
                          ThemeRepository themeRepository,
                          ReservationTimeRepository reservationTimeRepository,
                          WaitingRepository waitingRepository) {
        this.memberRepository = memberRepository;
        this.themeRepository = themeRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.waitingRepository = waitingRepository;
    }

    public WaitingResult create(CreateWaitingParam createWaitingParam) {
        ReservationTime reservationTime = reservationTimeRepository.findById(createWaitingParam.timeId()).orElseThrow(
                () -> new NotFoundReservationTimeException(createWaitingParam.timeId() + "에 해당하는 정보가 없습니다."));
        Theme theme = themeRepository.findById(createWaitingParam.themeId()).orElseThrow(
                () -> new NotFoundThemeException(createWaitingParam.themeId() + "에 해당하는 정보가 없습니다."));
        Member member = memberRepository.findById(createWaitingParam.memberId()).orElseThrow(
                () -> new NotFoundMemberException(createWaitingParam.memberId() + "에 해당하는 정보가 없습니다."));
        LocalDate date = createWaitingParam.date();

        //TODO: 중복 대기 검증

        int maxOrder = waitingRepository.findMaxOrderByThemeIdAndDateAndTimeId(createWaitingParam.themeId(), date, createWaitingParam.timeId());
        Waiting waiting = waitingRepository.save(
                Waiting.createNew(
                        member,
                        createWaitingParam.date(),
                        reservationTime,
                        theme,
                        maxOrder + 1
                )
        );
        return WaitingResult.from(waiting);
    }
}
