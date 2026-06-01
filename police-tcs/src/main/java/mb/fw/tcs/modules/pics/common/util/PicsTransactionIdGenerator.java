package mb.fw.tcs.modules.pics.common.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.RandomStringUtils;

public class PicsTransactionIdGenerator {
	private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public static String generate() {
        String timestamp = LocalDateTime.now().format(formatter);

        String randomPart = RandomStringUtils.randomAlphanumeric(8);

        return timestamp + randomPart;
    }
}
