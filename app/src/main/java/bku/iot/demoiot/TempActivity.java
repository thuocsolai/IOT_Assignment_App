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

public class TempActivity extends AppCompatActivity{

    private LineChart TempChart;
    private ImageView TempChartBack;
    String [] TempList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.temp_activity);

        TempChart = findViewById(R.id.TempChart);
        TempChartBack = findViewById(R.id.TempChartBack);

        TempList = getIntent().getStringArrayExtra("TempList");

        List<Entry> entries = new ArrayList<Entry>();
        for (int i = 0; i< TempList.length; i++) {
            float xValue = (float) i;
            float yValue = Float.parseFloat(TempList[i]);
            Log.d("TEST",  "Temp Values:" + yValue);
            entries.add(new Entry(xValue, yValue));
        }

        LineDataSet dataSet = new LineDataSet(entries, "Temp");
        dataSet.setDrawFilled(true);
        dataSet.setMode((LineDataSet.Mode.CUBIC_BEZIER));

        LineData lineData = new LineData(dataSet);

        TempChart.setData(lineData);
        TempChart.invalidate();
        TempChartBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

}