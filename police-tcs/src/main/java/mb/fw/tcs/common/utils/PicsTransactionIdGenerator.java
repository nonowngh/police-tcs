package mb.fw.tcs.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.RandomStringUtils;

public class PicsTransactionIdGenerator {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public static String generate() {
        // 1. 타임스탬프 (17자리)
        String timestamp = LocalDateTime.now().format(formatter);

        // 2. 랜덤 8자리 (대소문자 + 숫자 혼합)
        String randomPart = RandomStringUtils.randomAlphanumeric(8);

        return timestamp + randomPart;
    }
}
