package seu.virtualcampus.ui.bank;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

public class bank_utils {

    public static List<String> userAccountNumbers = new ArrayList<>();
    @Setter
    @Getter
    public static String currentAccountNumber;
    @Getter
    public static String studentId;

    public static void addAccountNumber(String accountNumber) {
        if (!userAccountNumbers.contains(accountNumber)) {
            userAccountNumbers.add(accountNumber);
        }
    }
}
