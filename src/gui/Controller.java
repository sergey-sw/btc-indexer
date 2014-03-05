package gui;

import model.ActivationFunctionType;
import model.DemoValuesBuilder;

/**
 * Author: Sergey42
 * Date: 14.02.14 23:20
 */
public class Controller {

    private long _teachStartTime;

    private View view;
    private Model model;
    private boolean networkCreated;
    private boolean networkStudied;

    public void onTeachIterationPerformed(int iteration) {
        //Определение интервала времени эпохи обучения
        double spentTime = System.currentTimeMillis() - _teachStartTime;
        view.displayTeachInfo(iteration, spentTime);
    }

    public void buildNet() {
        Object[] data = new Object[8];
        int[] layerList = (int[]) data[0];
        ActivationFunctionType[] activationFunctionTypes = (ActivationFunctionType[]) data[1];
        double[] params = (double[]) data[2];

        double xmin = (Double) data[3];
        double xmax = (Double) data[4];
        int n = (Integer) data[5];

        ActivationFunctionType activationFunctionTypeDemo = (ActivationFunctionType) data[7];
        double demoCoeff = (Double) data[8];

        try {

            model.createNetwork(layerList, activationFunctionTypes, params);

        } catch (Exception ex) {
            String _message = ex.getMessage();
            view.displayError(_message);
            return;
        }


        DemoValuesBuilder.sigma = demoCoeff;
        double[] functionValues = DemoValuesBuilder.getActivationFunctionDemoValues(xmin, xmax, n, activationFunctionTypeDemo);
        double[] derivateData = DemoValuesBuilder.getActivationFunctionDerivationDemoValues(xmin, xmax, n, activationFunctionTypeDemo);

        Object datas = new Object[]{xmin, xmax, n};

        view.drawActivationFunction(functionValues, datas);
        view.drawDerivation(derivateData, datas);

        networkCreated = true;
        networkStudied = false;

        view.displayNetworkCreatedMessage();
    }

    public void btnForecastClickHandler() {
        Object[] data = view.getTab2Data();
        if (data == null) return;

        int forecastDataSize = (Integer) data[3];
        //var studyDataSize = (int) data[4];

        double[] forecastValues = model.forecast(forecastDataSize);

        int inputLength = model.network.inputsMLP.length;
        int fullLength = model.network.fullLength;
        int studyLength = model.network.studyLength;

        double[] forecastDataArray = model.network.forecastInputs;

        int iLength = model.network.inputs.length;
        double[] fullArray = new double[iLength + forecastDataArray.length];
        System.arraycopy(model.network.inputs, 0, fullArray, 0, iLength);
        System.arraycopy(forecastDataArray, 0, fullArray, iLength, forecastDataArray.length);

        view.drawInputFunction(fullArray);
        view.drawForecast(forecastValues, inputLength, forecastDataArray);
    }

    public void BtnStudyClickHandler() {
        Object[] data = view.getTab2Data();
        if (data == null) return;

        double step = (Double) data[0];
        int teachCycleCount = (Integer) data[1];
        double speedRate = (Double) data[2];

        int studyDataSize = (Integer) data[4];
        int dataType = (Integer) data[5];
        boolean useMoments = (Boolean) data[6];

        model.network.teachCycleCount = teachCycleCount;
        model.network.useMoments = useMoments;

        view.initProgressBar(teachCycleCount);

        _teachStartTime = System.currentTimeMillis();

        model.network.speedRate = speedRate;

        model.teachNetwork(studyDataSize);

        double[] averageMistakeData = model.network.averageDiffPerEraHistory;
        view.drawAverageMistake(averageMistakeData);

        view.addStudyCbItems(teachCycleCount);

        networkStudied = true;
        view.displayFinishStudyMessage();
    }

    public void BtnMistakeClickHandler() {
        // Получение информации
        Object[] data = view.GetAnalysisData();

        if (data == null) return;

        int cycleNumber = (Integer) data[0];

        double[] reals = model.network.nInputs;
        //new double[_netModel.Network.OutputHistory[0].Length];
        double[] values = model.network.outputsHistory[cycleNumber];
        double[] diffs = model.network.differenceHistory[cycleNumber];

        view.drawMistakes(values, reals, diffs);
    }

    public void BtnShowInputGraphClickHandler() {
        Object[] data = view.getDataForInputGraph();
        if (data == null) return;

        int inputDataArrayLength = (Integer) data[1];

        model.initInputData(inputDataArrayLength);


        view.drawInputFunction(model.network.inputs);
    }
}

