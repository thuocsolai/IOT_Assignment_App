package bku.iot.demoiot;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import java.util.ArrayList;
import java.util.List;

public class HumidActivity extends AppCompatActivity{

    private LineChart HumidChart;
    private ImageView HumidChartBack;
    String [] HumidList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.humid_activity);

        HumidChart = findViewById(R.id.HumidChart);
        HumidChartBack = findViewById(R.id.HumidChartBack);

        HumidList = getIntent().getStringArrayExtra("HumidList");

        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i< HumidList.length; i++) {
            float xValue = (float) i;
            float yValue = Float.parseFloat(HumidList[i]);
            Log.d("TEST",  "Humid Values:" + yValue);
            entries.add(new Entry(xValue, yValue));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Humid");
        dataSet.setDrawFilled(true);
        dataSet.setMode((LineDataSet.Mode.CUBIC_BEZIER));

        LineData lineData = new LineData(dataSet);

        HumidChart.setData(lineData);
        HumidChart.invalidate();
        HumidChartBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}
