import model.IndexSnapshot;
import model.SnapshotMode;
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
        List<IndexSnapshot> indexSnapshotsClosePrice = webDataLoader.loadCoinDeskIndexes("2014-01-01", "2014-02-01", SnapshotMode.CLOSING_PRICE);
        List<IndexSnapshot> indexSnapshotsOHLC = webDataLoader.loadCoinDeskIndexes("2014-01-01", "2014-02-01", SnapshotMode.OHLC);

        if (indexSnapshotsClosePrice.isEmpty() || indexSnapshotsOHLC.isEmpty()) {
            throw new RuntimeException("Snapshots are empty");
        }
    }
}
