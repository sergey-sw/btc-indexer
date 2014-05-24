import com.ssau.btc.model.IndexSnapshot;
import com.ssau.btc.sys.DatabaseAPI;
import com.ssau.btc.sys.DatabaseManager;
import com.ssau.btc.utils.DateUtils;
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
        DatabaseAPI databaseAPI = new DatabaseManager();
        try {
            databaseAPI.testSettings();
            System.out.println("Before");
            List<IndexSnapshot> dailyIndexes = databaseAPI.getDailyIndexes();
            for (IndexSnapshot indexSnapshot : dailyIndexes) {
                System.out.println(indexSnapshot);
            }

            databaseAPI.storeDailyIndexes(Arrays.asList(new IndexSnapshot(DateUtils.getDate("2018-02-03"), 24.03)));
            System.out.println("After insert");
            dailyIndexes = databaseAPI.getDailyIndexes();
            for (IndexSnapshot indexSnapshot : dailyIndexes) {
                System.out.println(indexSnapshot);
            }

            databaseAPI.removeDailyIndexes(Arrays.asList(DateUtils.getDate("2018-02-03")));
            System.out.println("After delete");
            dailyIndexes = databaseAPI.getDailyIndexes();
            for (IndexSnapshot indexSnapshot : dailyIndexes) {
                System.out.println(indexSnapshot);
            }

        } catch (Exception e) {
            System.out.println("Failed to find driver: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
