import com.ssau.btc.sys.CurrentPriceProvider;
import org.junit.Test;

/**
 * Author: Sergey42
 * Date: 05.04.14 18:24
 */
public class CurrentPriceTest {

    @Test
    public void run() {
        System.out.println(new CurrentPriceProvider().getCurrentPrice());
    }
}
