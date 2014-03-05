import model.IndexSnapshot;
import org.junit.Test;
import sys.WebDataLoader;

import java.util.List;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public class WebDataLoadTest {

    @Test
    public void run() {
        WebDataLoader webDataLoader = new WebDataLoader();
        List<IndexSnapshot> indexSnapshots = webDataLoader.loadCoinDeskIndexes("2014-01-01", "2014-02-01");

        if (indexSnapshots.isEmpty()) {
            throw new RuntimeException("Snapshots are empty");
        }
    }
}
