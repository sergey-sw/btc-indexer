package com.ssau.btc.gui;

import com.ssau.btc.model.ActivationFunctionType;
import com.ssau.btc.model.LayerInfo;

import java.util.ArrayList;
import java.util.List;

/**
 * Author: Sergey42
 * Date: 15.02.14 15:26
 */
public class Config {

    public static final int MAX_LAYER_NEURON_CNT = 100;

    public static final String DEFAULT_ERA_CNT = "25";
    public static final String DEFAULT_TEACH_COEFF = "0.2";
    public static final String MIN_DATE_FROM = "2013-01-01";
    public static final int MAX_ERA_COUNT = 100;

    public static List<LayerInfo> getDefaultStructure() {
        List<LayerInfo> items = new ArrayList<>();
        items.add(new LayerInfo(8, null, 0));
        items.add(new LayerInfo(16, ActivationFunctionType.H_TANGENT, 0.15));
        items.add(new LayerInfo(1, ActivationFunctionType.SINUS, 0.5));
        return items;
    }

    public static LayerInfo createLayerInfo() {
        return new LayerInfo(10, null, 0.8);
    }
}