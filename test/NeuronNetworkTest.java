import com.ssau.btc.model.IndexSnapshot;
import com.ssau.btc.model.NetworkAPI;
import com.ssau.btc.model.NetworkCreator;
import com.ssau.btc.model.SnapshotMode;
import com.ssau.btc.sys.WebDataLoader;
import com.ssau.btc.sys.WebLoaderAPI;
import com.ssau.btc.utils.IndexSnapshotUtils;
import org.junit.Test;

import java.util.Collection;

/**
 * Author: Sergey42
 * Date: 05.03.14 21:01
 */
public class NeuronNetworkTest {

    @Test
    public void run() {
        NetworkAPI network = createNetwork();

        WebDataLoader dataLoader = new WebDataLoader();
        Collection<IndexSnapshot> indexSnapshots = dataLoader.loadCoinDeskIndexes
                ("2014-01-01", "2014-03-01", SnapshotMode.CLOSING_PRICE, WebLoaderAPI.DAY);

        double[] data = IndexSnapshotUtils.parseClosingPrice(indexSnapshots);
        network.initInputData(data);

        network.setValue("speedRate", 0.7);
        network.setValue("teachCycleCount", 50);

        network.teach();

        double[] forecasts = network.forecast(30);
        for (double forecast : forecasts) {
            System.out.println("forecast: " + forecast);
        }
    }

    private NetworkAPI createNetwork() {
        return NetworkCreator.buildDefault();
    }
}
