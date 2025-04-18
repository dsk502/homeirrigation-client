package com.simon.homeirrigationclient.ui.machine.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.simon.homeirrigationclient.HICApplication;
import com.simon.homeirrigationclient.R;
import com.simon.homeirrigationclient.databinding.FragmentStatisticsBinding;
import com.simon.homeirrigationclient.model.DeviceInfo;
import com.simon.homeirrigationclient.model.WateringRecordDatabaseHelper;
import com.simon.homeirrigationclient.ui.machine.dashboard.DashboardViewModel;

import java.io.File;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class StatisticsFragment extends Fragment {

    private FragmentStatisticsBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        StatisticsViewModel statisticsViewModel =
                new ViewModelProvider(this).get(StatisticsViewModel.class);

        binding = FragmentStatisticsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        DashboardViewModel dashboardViewModel = new ViewModelProvider(requireActivity()).get(DashboardViewModel.class);

        //Determine whether the watering record database file exists
        DeviceInfo currentDeviceInfo = HICApplication.getInstance().servers.get(dashboardViewModel.indexOfDeviceInfo);
        File wateringRecordFile = requireContext().getDatabasePath("watering_record_" + currentDeviceInfo.serverId + ".db");
        if(wateringRecordFile.exists()) {
            currentDeviceInfo.wateringRecordDatabaseHelper = new WateringRecordDatabaseHelper(requireContext(), "watering_record_" + currentDeviceInfo.serverId + ".db");
            displayStat(currentDeviceInfo.wateringRecordDatabaseHelper);
        }

        //Set the event for clicking the button "Refresh"
        Button buttonRefresh = view.findViewById(R.id.button_stat_refresh);
        buttonRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Download stat
                //currentDeviceInfo.wateringRecordDatabaseHelper = null;
                currentDeviceInfo.tcpClient.downloadStatRequest(currentDeviceInfo.serverId, currentDeviceInfo.serverPubkey);
                currentDeviceInfo.wateringRecordDatabaseHelper = new WateringRecordDatabaseHelper(requireContext(), "watering_record_" + currentDeviceInfo.serverId + ".db");

                //Display stat
                displayStat(currentDeviceInfo.wateringRecordDatabaseHelper);
            }
        });

        //Set the event for clicking "Delete"
        Button buttonDelete = view.findViewById(R.id.button_stat_delete);
        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Delete the data on the server
                currentDeviceInfo.tcpClient.deleteStatRequest(currentDeviceInfo.serverId, currentDeviceInfo.serverPubkey);
                //Delete the record helper object
                currentDeviceInfo.wateringRecordDatabaseHelper = null;
                //Delete the local record file
                wateringRecordFile.delete();
            }
        });
    }

    public void displayStat(WateringRecordDatabaseHelper recordHelper) {
        if(recordHelper != null) {
            //Get the water consumption barchart
            BarChart waterConsumBarChart = requireActivity().findViewById(R.id.water_consumption_barChart);
            HashMap<String, String> waterConsumption = recordHelper.getAmountOfWatering();
            setBarChart(waterConsumBarChart, waterConsumption, "Water Consumption");

            LineChart timesOfWateringLineChart = requireActivity().findViewById(R.id.times_of_watering_lineChart);
            HashMap<String, String> timesOfWatering = recordHelper.getTimesOfWatering();
            setLineChart(timesOfWateringLineChart, timesOfWatering, "Times of watering");

            LineChart soilMoistureLineChart = requireActivity().findViewById(R.id.soil_moisture_lineChart);
            HashMap<String, String> soilMoisture = recordHelper.getSoilMoisturePercentage();
            setLineChart(soilMoistureLineChart, soilMoisture, "Soil Moisture Percentage");
        }
    }

    private void setLineChart(LineChart lineChart, HashMap<String, String> data, String dataSetLabel) {
        List<Entry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String date = entry.getKey();
            float value = Float.parseFloat(entry.getValue());
            entries.add(new Entry(index, value));
            dates.add(date);
            index++;
        }

        // 创建 LineDataSet
        LineDataSet dataSet = new LineDataSet(entries, dataSetLabel); // 添加标签
        dataSet.setColor(Color.BLUE); // 设置折线颜色
        dataSet.setLineWidth(2f); // 设置折线宽度
        dataSet.setCircleColor(Color.RED); // 设置数据点颜色
        dataSet.setCircleRadius(5f); // 设置数据点大小
        dataSet.setValueTextSize(10f); // 设置数据点文本大小
        dataSet.setValueTextColor(Color.BLACK); // 设置数据点文本颜色

        // 创建 LineData
        LineData lineData = new LineData(dataSet);

        // 设置数据到 LineChart
        lineChart.setData(lineData);

        // 自定义 X 轴显示日期
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < dates.size()) {
                    return dates.get(index);
                }
                return "";
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // 最小间隔为 1
        xAxis.setGranularityEnabled(true); // 禁止自动调整间隔
        // 启用横向滚动
        lineChart.setScaleEnabled(true); // 启用缩放
        lineChart.setDragEnabled(true);  // 启用拖动
        lineChart.setPinchZoom(false);   // 禁用双指缩放
        // 刷新图表
        lineChart.invalidate();
    }

    private void setBarChart(BarChart barChart, HashMap<String, String> data, String dataSetLabel) {
        List<BarEntry> entries = new ArrayList<>();
        List<String> dates = new ArrayList<>();
        int index = 0;
        for (Map.Entry<String, String> entry : data.entrySet()) {
            String date = entry.getKey();
            float value = Float.parseFloat(entry.getValue());
            entries.add(new BarEntry(index, value));
            dates.add(date);
            index++;
        }

        // 创建 LineDataSet
        BarDataSet dataSet = new BarDataSet(entries, dataSetLabel); // 添加标签
        dataSet.setColor(Color.BLUE); // 设置折线颜色
        dataSet.setValueTextSize(10f); // 设置数据点文本大小
        dataSet.setValueTextColor(Color.BLACK); // 设置数据点文本颜色

        // 创建 LineData
        BarData barData = new BarData(dataSet);

        // 设置数据到 LineChart
        barChart.setData(barData);

        // 自定义 X 轴显示日期
        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < dates.size()) {
                    return dates.get(index);
                }
                return "";
            }
        });

        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f); // 最小间隔为 1
        xAxis.setGranularityEnabled(true); // 禁止自动调整间隔

        barChart.setScaleEnabled(true); // 启用缩放
        barChart.setDragEnabled(true);  // 启用拖动
        barChart.setPinchZoom(false);   // 禁用双指缩放

        // 刷新图表
        barChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}