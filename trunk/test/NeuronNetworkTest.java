import com.ssau.btc.gui.AppFrame;
import com.ssau.btc.gui.Config;
import com.ssau.btc.model.*;
import com.ssau.btc.sys.WebDataLoader;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertNotNull;

/**
 * Author: Sergey42
 * Date: 05.03.14 21:01
 */
public class NeuronNetworkTest {

    @Test
    public void run() {
        NetworkAPI network = createNetwork();
        assertNotNull(network);

        WebDataLoader dataLoader = new WebDataLoader();
        List<IndexSnapshot> indexSnapshots = dataLoader.loadCoinDeskIndexes("2014-01-01", "2014-03-01", SnapshotMode.CLOSING_PRICE);

        double[] data = IndexSnapshotUtils.parseClosingPrice(indexSnapshots);
        network.initInputData(data);

        network.setValue("speedRate", 0.7);
        network.setValue("teachCycleCount", 50);

        network.teach();

        double[] forecasts = network.fuzzyForecast(10);
        for (double forecast : forecasts) {
            System.out.println(forecast);
        }
    }

    private NetworkAPI createNetwork() {
        List<AppFrame.TableItem> defaultStructure = Config.getDefaultStructure();

        int size = defaultStructure.size();

        int[] neuronCounts = new int[size];
        ActivationFunctionType[] types = new ActivationFunctionType[size];
        double[] coefficients = new double[size];

        for (int i = 0; i < size; i++) {
            neuronCounts[i] = defaultStructure.get(i).neuronCnt;
            types[i] = defaultStructure.get(i).functionType;
            coefficients[i] = defaultStructure.get(i).coefficient;
        }

        return NetworkCreator.create(neuronCounts, types, coefficients);
    }
}
