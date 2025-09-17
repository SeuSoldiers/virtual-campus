package seu.virtualcampus.ui.bank;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * 银行工具类。
 * <p>
 * 提供账户号管理等静态工具方法。
 * </p>
 */
public class bank_utils {

    public static List<String> userAccountNumbers = new ArrayList<>();
    @Setter
    @Getter
    public static String currentAccountNumber;
    @Getter
    public static String studentId;

    /**
     * 添加账户号到用户账户列表。
     *
     * @param accountNumber 账户号。
     */
    public static void addAccountNumber(String accountNumber) {
        if (!userAccountNumbers.contains(accountNumber)) {
            userAccountNumbers.add(accountNumber);
        }
    }
}
