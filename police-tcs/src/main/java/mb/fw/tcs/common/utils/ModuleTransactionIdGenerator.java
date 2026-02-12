package mb.fw.tcs.common.utils;

public class ModuleTransactionIdGenerator {

	public static String generate(String interfaceId, String alias) {
        return interfaceId + "_" +  alias;
    }
}
