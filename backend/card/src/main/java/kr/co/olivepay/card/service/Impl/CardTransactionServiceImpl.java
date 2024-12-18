package kr.co.olivepay.card.service.Impl;

import jakarta.annotation.PostConstruct;
import kr.co.olivepay.card.entity.Card;
import kr.co.olivepay.card.entity.CardCompany;
import kr.co.olivepay.card.global.enums.ErrorCode;
import kr.co.olivepay.card.global.handler.AppException;
import kr.co.olivepay.card.repository.CardCompanyRepository;
import kr.co.olivepay.card.repository.CardRepository;
import kr.co.olivepay.card.service.CardTransactionService;
import kr.co.olivepay.core.card.dto.req.CardSearchReq;
import kr.co.olivepay.core.card.dto.res.enums.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CardTransactionServiceImpl implements CardTransactionService {

    private final CardRepository cardRepository;
    private final CardCompanyRepository cardCompanyRepository;

    /**
     * 카드 등록
     *
     * @param card
     * @return 등록된 카드 반환
     */
    public Card registerCard(final Card card) {
        return cardRepository.save(card);
    }

    /**
     * 카드사 이름으로 카드사 객체 반환
     *
     * @param cardCompanyName
     * @return
     */
    public CardCompany getCardCompany(final String cardCompanyName) {
        return cardCompanyRepository.findByName(cardCompanyName)
                                    .orElseThrow(() -> new AppException(ErrorCode.CARDCOMPANY_NOT_EXIST));
    }

    /**
     * 실제 카드번호로 카드객체 반환 Optional
     *
     * @param realCardNumber
     * @return
     */
    public Optional<Card> getCard(final String realCardNumber) {
        Optional<Card> optionalCard = cardRepository.findByRealCardNumber(realCardNumber);
        return optionalCard;
    }

    /**
     * 해당 멤버의 isDefault카드 객체 반환
     *
     * @param memberId
     * @return
     */
    @Override
    public Optional<Card> getDefaultCard(final Long memberId) {
        Optional<Card> optionalCard = cardRepository.findByMemberIdAndIsDefaultTrue(memberId);
        return optionalCard;
    }

    /**
     * 카드 삭제 로직
     * 삭제된 레코드가 0개이면 카드 존재 X 예외처리
     *
     * @param memberId
     * @param cardId
     */
    @Transactional
    public void deleteCard(final Long memberId, final Long cardId) {
        Card card = cardRepository.findById(cardId)
                                  .orElseThrow(() -> new AppException(ErrorCode.CARD_NOT_EXIST));
        if (card.getCardType() == CardType.DREAMTREE) {
            throw new AppException(ErrorCode.DREAM_CARD_CAN_NOT_DELETE);
        }
        int cnt = cardRepository.deleteByIdAndMemberId(cardId, memberId);
        if (cnt == 0) {
            throw new AppException(ErrorCode.BAD_REQUEST);
        }
    }

    /**
     * 멤버 ID를 통해 해당 멤버의 전체 카드 엔티티를 반환
     *
     * @param memberId
     * @return 해당 멤버의 모든 카드를 반환
     */
    @Transactional(readOnly = true)
    public List<Card> getMyCardList(final Long memberId) {
        return cardRepository.findByMemberIdOrderByIsDefaultDesc(memberId);
    }

    /**
     * 해당 멤버의 꿈나무 카드,
     * 조건에 따라 (차액 결제 카드, 공용 기부금 카드) 반환
     *
     * @param memberId
     * @param cardSearchReq
     * @return 꿈나무 카드, 차액 결제 카드, 공용 기부금 카드
     */
    @Transactional(readOnly = true)
    public List<Card> getPaymentCardList(final Long memberId, CardSearchReq cardSearchReq) {
        return cardRepository.findByMemberIdAndCardSearchReq(memberId, cardSearchReq);
    }

    @Override
    public Optional<Card> getCardWithAccountById(final Long cardId) {
        return cardRepository.findCardAndAccountById(cardId);
    }


    /**
     * 카드사 초기 생성을 위한 작업
     */
    @PostConstruct
    @Transactional
    public void init() {
        List<CardCompany> cardCompanyList = cardCompanyRepository.findAll();
        if (!cardCompanyList.isEmpty()) {
            return;
        }
        String[] names = {
                "KB국민카드", "삼성카드", "롯데카드", "우리카드",
                "신한카드", "꿈나무카드", "현대카드", "BC카드", "NH농협카드",
                "하나카드", "IBK기업은행카드"
        };
        for (int i = 0; i < 10; i++) {
            CardCompany cardCompany = CardCompany.builder()
                                                 .name(names[i])
                                                 .build();
            cardCompanyRepository.save(cardCompany);
        }
    }
}
