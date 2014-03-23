import com.ssau.btc.sys.DataManager;
import org.junit.Test;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public class PgConnectionTest {

    @Test
    public void run() {
        DataManager dataManager = new DataManager();
        try {
            dataManager.testSettings();
        } catch (Exception e) {
            System.out.println("Failed to find driver: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }
}
