import com.ssau.btc.gui.Config;
import com.ssau.btc.model.*;
import com.ssau.btc.sys.WebDataLoader;
import org.junit.Test;

import java.util.Collection;
import java.util.List;

/**
 * Author: Sergey42
 * Date: 05.03.14 21:01
 */
public class NeuronNetworkTest {

    @Test
    public void run() {
        NetworkAPI network = createNetwork();

        WebDataLoader dataLoader = new WebDataLoader();
        Collection<IndexSnapshot> indexSnapshots = dataLoader.loadCoinDeskIndexes("2014-01-01", "2014-03-01", SnapshotMode.CLOSING_PRICE);

        double[] data = IndexSnapshotUtils.parseClosingPrice(indexSnapshots);
        network.initInputData(data, Interval.DAY);

        network.setValue("speedRate", 0.7);
        network.setValue("teachCycleCount", 50);
        network.setValue("studyLength", data.length);

        network.teach();

        double[] forecasts = network.fuzzyForecast(30);
        for (double forecast : forecasts) {
            System.out.println("forecast: " + forecast);
        }
    }

    private NetworkAPI createNetwork() {
        List<LayerInfo> defaultStructure = Config.getDefaultStructure();

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
