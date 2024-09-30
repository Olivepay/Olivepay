package kr.co.olivepay.core.transaction.topic;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class Topic {

    //결제 프로세스 시작
    public final static String PAYMENT_PENDING = "PAYMENT_PENDING";
    //결제 프로세스 완료
    public final static String PAYMENT_COMPLETE = "PAYMENT_COMPLETE";
    //결제 프로세스 실패
    public final static String PAYMENT_FAIL = "PAYMENT_FAIL";

    //잔액 체크
    public final static String ACCOUNT_BALANCE_CHECK = "ACCOUNT_BALANCE_CHECK";
    //잔액 체크 성공
    public static final String ACCOUNT_BALANCE_CHECK_SUCCESS = "ACCOUNT_BALANCE_CHECK_SUCCESS";
    //잔액 체크 실패
    public static final String ACCOUNT_BALANCE_CHECK_FAIL = "ACCOUNT_BALANCE_CHECK_FAIL";

    //결제 요청
    public final static String PAYMENT_APPLY = "PAYMENT_APPLY";
    //결제 성공
    public final static String PAYMENT_APPLY_SUCCESS = "PAYMENT_APPLY_SUCCESS";
    //결제 실패
    public final static String PAYMENT_APPLY_FAIL = "PAYMENT_APPLY_FAIL";

    //쿠폰 적용 요청
    public final static String COUPON_APPLY = "COUPON_APPLY";
    //쿠폰 적용 성공
    public final static String COUPON_APPLY_SUCCESS = "COUPON_APPLY_SUCCESS";
    //쿠폰 적용 실패
    public final static String COUPON_APPLY_FAIL = "COUPON_APPLY_FAIL";
}

