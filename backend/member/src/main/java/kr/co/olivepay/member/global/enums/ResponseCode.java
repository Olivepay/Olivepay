package kr.co.olivepay.member.global.enums;

import org.springframework.http.HttpStatus;

public interface ResponseCode {

    HttpStatus getHttpStatus();

    String getMessage();
}
