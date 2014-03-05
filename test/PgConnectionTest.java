import org.junit.Test;
import sys.DataManager;

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
