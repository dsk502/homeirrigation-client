package com.simon.homeirrigationclient.ui.machine.statistics;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

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
import com.simon.homeirrigationclient.model.TCPClient;
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
                int downloadStatResult = currentDeviceInfo.tcpClient.downloadStatRequest(currentDeviceInfo.serverId, currentDeviceInfo.serverPubkey);
                switch(downloadStatResult) {
                    case TCPClient.ERR_NETWORK_TIMEOUT:
                        Toast.makeText(requireContext(), "Error: Network timeout", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_MESSAGE_FORMAT:
                        Toast.makeText(requireContext(), "Error: Message format is wrong", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_MESSAGE_CONTENT:
                        Toast.makeText(requireContext(), "Error: Message content is wrong", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_INTERRUPTED:
                        Toast.makeText(requireContext(), "Error: Device added", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_OTHERS:
                        Toast.makeText(requireContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                        break;
                }

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
                int delDataResult = currentDeviceInfo.tcpClient.deleteStatRequest(currentDeviceInfo.serverId, currentDeviceInfo.serverPubkey);
                switch(delDataResult) {
                    case 0:
                        //Delete the record helper object
                        currentDeviceInfo.wateringRecordDatabaseHelper = null;
                        //Delete the local record file
                        wateringRecordFile.delete();
                        break;
                    case TCPClient.ERR_NETWORK_TIMEOUT:
                        Toast.makeText(requireContext(), "Error: Network timeout", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_MESSAGE_FORMAT:
                        Toast.makeText(requireContext(), "Error: Message format is wrong", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_MESSAGE_CONTENT:
                        Toast.makeText(requireContext(), "Error: Message content is wrong", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_INTERRUPTED:
                        Toast.makeText(requireContext(), "Error: Device added", Toast.LENGTH_SHORT).show();
                        break;
                    case TCPClient.ERR_OTHERS:
                        Toast.makeText(requireContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                        break;
                }
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

        //Create LineDataSet
        LineDataSet dataSet = new LineDataSet(entries, dataSetLabel); //Add label
        dataSet.setColor(Color.BLUE); //Set line color
        dataSet.setLineWidth(2f); //Set line width
        dataSet.setCircleColor(Color.RED); //Set data point color
        dataSet.setCircleRadius(5f); //Set data point size
        dataSet.setValueTextSize(10f); //Set text size
        dataSet.setValueTextColor(Color.BLACK); // Set text color

        //Create LineData
        LineData lineData = new LineData(dataSet);

        //Bind the data to LineChart
        lineChart.setData(lineData);

        //Set date on x axis
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
        xAxis.setGranularity(1f); // Min gap is 1
        xAxis.setGranularityEnabled(true); // No adjusting gap
        //Scroll horizontally
        lineChart.setScaleEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setPinchZoom(false);
        //Refresh the chart
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

        //Create BarDataSet
        BarDataSet dataSet = new BarDataSet(entries, dataSetLabel); //Add label
        dataSet.setColor(Color.BLUE); // Set bar color
        dataSet.setValueTextSize(10f); // Set text size
        dataSet.setValueTextColor(Color.BLACK); // Set text color

        //Create BarData
        BarData barData = new BarData(dataSet);

        //Bind the data to BarChart
        barChart.setData(barData);

        //Show date on x axis
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
        xAxis.setGranularity(1f);
        xAxis.setGranularityEnabled(true);

        barChart.setScaleEnabled(true);
        barChart.setDragEnabled(true);
        barChart.setPinchZoom(false);

        barChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}