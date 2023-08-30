package hec.ensembleview.charts;

import hec.gfx2d.G2dLineProperties;
import hec.gfx2d.G2dPanel;
import hec.gfx2d.PairedDataSet;
import hec.gfx2d.ViewportLayout;
import hec.io.PairedDataContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EnsembleChartAcrossEnsembles extends EnsembleChart {
    private String y2Label = "";
    private final List<PairedDataSet> pairedDataSetListY1 = new ArrayList<>();
    private final List<PairedDataSet> pairedDataSetListY2 = new ArrayList<>();
    private final List<G2dLineProperties> g2dPointPropertiesListY1 = new ArrayList<>();
    private final List<G2dLineProperties> g2dPointPropertiesListY2 = new ArrayList<>();
    private double[] xOrdinates;
    private double[][] yOrdinates;

    /**
     * Ensembles Charts Across Ensembles class sets up and displays the metrics for the scatter plot chart
     */

    public void setY2Label(String label) {
        y2Label = label;
    }

    private void setXyOrdinates(double[] values, double[] prob) {
        xOrdinates = new double[values.length];
        yOrdinates = new double[1][values.length];

        if (prob.length > 0) {
            xOrdinates = prob;
            for (int i = 0; i < values.length; i++) {
                yOrdinates[0][i] = values[i];
            }
        } else {
            for (int i = 0; i < values.length; i++) {
                xOrdinates[i] = i + 1d;
                yOrdinates[0][i] = values[i];
            }
        }
    }

    public void addPoint(PointSpec point) {
        PairedDataContainer pairedDataContainer = new PairedDataContainer();
        double[] values = floatToDoubleConversion(point.yValue);
        setXyOrdinates(values, new double[0]);

        pairedDataContainer.setValues(xOrdinates, yOrdinates);
        pairedDataContainer.setName(point.pointName);

        PairedDataSet pairedDataSet = new PairedDataSet(pairedDataContainer);
        pairedDataSet.setName(point.pointName);

        if(point.rangeAxis.equalsIgnoreCase(ViewportLayout.Y1)) {
            pairedDataSetListY1.add(pairedDataSet);
        } else {
            pairedDataSetListY2.add(pairedDataSet);
        }

        G2dLineProperties props = new G2dLineProperties();
        setG2dPointProperties(props, point);
    }

    public void addProbPoint(PointSpec point) {
        //get probability map and set to double array
        Map<Float, Float> probValues = point.prob;
        List<Float> statValues = new ArrayList<>(probValues.values());
        List<Float> probability = new ArrayList<>(probValues.keySet());

        double[] values = statValues.stream().mapToDouble(Float::doubleValue).toArray();
        double[] prob = probability.stream().mapToDouble(Float::doubleValue).toArray();

        setXyOrdinates(values, prob);

        // Create new pairedDataContainer and add x and y ordinates
        PairedDataContainer pairedDataContainer = new PairedDataContainer();
        pairedDataContainer.setValues(xOrdinates, yOrdinates);
        pairedDataContainer.setName(point.pointName);

        // Create new PairedDatedSet and add to arraylist based on axis
        PairedDataSet pairedDataSet = new PairedDataSet(pairedDataContainer);
        pairedDataSet.setName(point.pointName);

        if(point.rangeAxis.equalsIgnoreCase(ViewportLayout.Y1)) {
            pairedDataSetListY1.add(pairedDataSet);
        } else {
            pairedDataSetListY2.add(pairedDataSet);
        }

        // Create G2dLineProperties
        G2dLineProperties props = new G2dLineProperties();
        setG2dPointProperties(props, point);
    }

    private void setG2dPointProperties(G2dLineProperties props, PointSpec pointSpec) {
        props.setDrawLine(false);
        props.setDrawPoints(true);
        props.setName(pointSpec.pointName);
        props.setLabel(pointSpec.pointName);
        props.setPointLineColor(pointSpec.pointColor);
        props.setSymbolFillColor(pointSpec.pointColor);

        if(pointSpec.rangeAxis.equalsIgnoreCase(ViewportLayout.Y1)) {
            g2dPointPropertiesListY1.add(props);
        } else {
            g2dPointPropertiesListY2.add(props);
        }
    }

    @Override
    public G2dPanel generateChart() {
        super.generateChart();

        if(view == null) {
            view = layout.addViewport(1.0);
            view.setAxisLabel(ViewportLayout.X1, xLabel);
        }
        buildViewPortGraph();

        plotPanel.buildComponents(layout);

        return plotPanel;
    }

    private void buildViewPortGraph() {
        if(!pairedDataSetListY1.isEmpty()) {
            for (int i = 0; i < pairedDataSetListY1.size(); i++) {
                view.addCurve(ViewportLayout.Y1, pairedDataSetListY1.get(i), g2dPointPropertiesListY1.get(i));
            }
            view.setAxisLabel(ViewportLayout.Y1, yLabel);
        }

        if(!pairedDataSetListY2.isEmpty()) {
            for (int i = 0; i < pairedDataSetListY2.size(); i++) {
                view.addCurve(ViewportLayout.Y2, pairedDataSetListY2.get(i), g2dPointPropertiesListY2.get(i));
            }
            view.setAxisName(ViewportLayout.Y2, y2Label);
        }
    }
}

