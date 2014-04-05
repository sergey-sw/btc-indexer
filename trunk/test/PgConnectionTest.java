import com.ssau.btc.model.IndexSnapshot;
import com.ssau.btc.sys.DatabaseAPI;
import com.ssau.btc.sys.DatabaseManager;
import com.ssau.btc.sys.DateUtils;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public class PgConnectionTest {

    @Test
    public void run() throws Exception {
        DatabaseAPI dataManager = new DatabaseManager();
        try {
            dataManager.testSettings();
            System.out.println("Before");
            List<IndexSnapshot> dailyIndexes = dataManager.getDailyIndexes();
            for (IndexSnapshot indexSnapshot : dailyIndexes) {
                System.out.println(indexSnapshot);
            }

            dataManager.storeDailyIndexes(Arrays.asList(new IndexSnapshot(DateUtils.getDate("2018-02-03"), 24.03)));
            System.out.println("After insert");
            dailyIndexes = dataManager.getDailyIndexes();
            for (IndexSnapshot indexSnapshot : dailyIndexes) {
                System.out.println(indexSnapshot);
            }

            dataManager.removeDailyIndexes(Arrays.asList(DateUtils.getDate("2018-02-03")));
            System.out.println("After delete");
            dailyIndexes = dataManager.getDailyIndexes();
            for (IndexSnapshot indexSnapshot : dailyIndexes) {
                System.out.println(indexSnapshot);
            }

        } catch (Exception e) {
            System.out.println("Failed to find driver: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
