import com.ssau.btc.model.IndexSnapshot;
import com.ssau.btc.model.SnapshotMode;
import com.ssau.btc.sys.WebDataLoader;
import org.junit.Test;

import java.util.Collection;

/**
 * @author Sergey Saiyan
 * @version $Id$
 */
public class WebDataLoadTest {

    @Test
    public void run() {
        WebDataLoader webDataLoader = new WebDataLoader();
        Collection<IndexSnapshot> indexSnapshotsClosePrice = webDataLoader.loadCoinDeskIndexes("2014-01-01", "2014-02-01", SnapshotMode.CLOSING_PRICE);
        Collection<IndexSnapshot> indexSnapshotsOHLC = webDataLoader.loadCoinDeskIndexes("2014-01-01", "2014-02-01", SnapshotMode.OHLC);

        if (indexSnapshotsClosePrice.isEmpty() || indexSnapshotsOHLC.isEmpty()) {
            throw new RuntimeException("Snapshots are empty");
        }
    }
}
