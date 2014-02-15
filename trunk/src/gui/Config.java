package gui;

import model.ActivationFunctionType;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Sergey42
 * Date: 15.02.14 15:26
 */
public class Config {

    public static List<AppFrame.TableItem> getDefaultStructure() {
        List<AppFrame.TableItem> items = new ArrayList<>();
        items.add(new AppFrame.TableItem(8, null, 0));
        items.add(new AppFrame.TableItem(16, ActivationFunctionType.H_TANGENT, 0.15));
        items.add(new AppFrame.TableItem(1, ActivationFunctionType.SINUS, 0.5));
        return items;
    }
}
