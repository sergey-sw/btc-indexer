package com.ssau.btc.model;

/**
 * Author: Sergey42
 * Date: 14.02.14 23:22
 */
public class DemoValuesBuilder {

    public static double sigma = 1;

    public static double[] getActivationFunctionDemoValues(double xmin, double xmax, int n, ActivationFunctionType type) {
        double[] result = new double[n];
        double h = (xmax - xmin) / n;

        switch (type) {
            case C_SIGMOID: {
                for (int i = 0; i < n; i++) {
                    double x = xmin + i * h;
                    result[i] = 1 / (1 + Math.exp(-sigma * x));
                }
                break;
            }
            case R_SIGMOID: {
                for (int i = 0; i < n; i++) {
                    double x = xmin + i * h;
                    result[i] = x / (Math.abs(x) + sigma);
                }
                break;
            }
            case H_TANGENT: {
                for (int i = 0; i < n; i++) {
                    double x = xmin + i * h;
                    double e2X = Math.exp(2 * sigma * x);
                    result[i] = (e2X - 1) / (e2X + 1);
                }
                break;
            }
            case SINUS: {
                for (int i = 0; i < n; i++) {
                    double x = xmin + i * h;
                    result[i] = Math.sin(sigma * x);
                }
                break;
            }
        }

        return result;
    }

    public static double[] getActivationFunctionDerivationDemoValues(double xmin, double xmax, int n, ActivationFunctionType functionType) {
        double[] result = new double[n];
        double h = (xmax - xmin) / n;

        switch (functionType) {

            case C_SIGMOID: {
                for (int i = 0; i < n; i++) {
                    double x = xmin + i * h;
                    double esx = Math.exp(-sigma * x);
                    result[i] = sigma * esx / Math.pow((1 + esx), 2);
                }
                break;
            }
            case R_SIGMOID: {
                for (int i = 0; i < n; i++) {
                    double x = xmin + i * h;
                    result[i] = (Math.abs(x) + x + sigma) / Math.pow(Math.abs(x) + sigma, 2);
                }
                break;
            }
            case H_TANGENT: {
                for (int i = 0; i < n; i++) {
                    double x = xmin + i * h;
                    double e2X = Math.exp(2 * sigma * x);
                    double fx = (e2X - 1) / (e2X + 1);
                    result[i] = sigma * (1 - fx * fx);
                }
                break;
            }
            case SINUS: {
                for (int i = 0; i < n; i++) {
                    double x = xmin + i * h;
                    result[i] = sigma * Math.cos(sigma * x);
                }
                break;
            }
        }
        return result;
    }
}
