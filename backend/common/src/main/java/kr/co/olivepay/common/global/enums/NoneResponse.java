package kr.co.olivepay.common.global.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NoneResponse {
    NONE("NONE");
    private final String message;
}
