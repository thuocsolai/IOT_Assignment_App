package bku.iot.demoiot;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Button;
import android.content.Intent;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

public class LightActivity extends AppCompatActivity{

//    private LineChart LightChart;
    MQTTHelper mqttHelper;
    String username = "IOT_232";
    private ImageView LightChartBack;
    private ImageView Confỉrm;
    private EditText cycleEditText;
    private EditText flow1EditText;
    private EditText flow2EditText;
    private EditText flow3EditText;
    private EditText startHourEditText;
    private EditText startMinuteEditText;
    private EditText endHourEditText;
    private EditText endMinuteEditText;
    public String cycle, flow1, flow2, flow3, hour1, minute1, hour2, minute2, area;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.light_activity);

        cycleEditText = findViewById(R.id.cycle);
        flow1EditText = findViewById(R.id.flow1);
        flow2EditText = findViewById(R.id.flow2);
        flow3EditText = findViewById(R.id.flow3);
        startHourEditText = findViewById(R.id.hour1);
        startMinuteEditText = findViewById(R.id.minute1);
        endHourEditText = findViewById(R.id.hour2);
        endMinuteEditText = findViewById(R.id.minute2);

        LightChartBack = findViewById(R.id.LightChartBack);
        Spinner spinner = findViewById(R.id.area_drop);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.area_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                area = parent.getItemAtPosition(position).toString();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        LightChartBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        Button confirmButton = findViewById(R.id.confirm);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cycle = cycleEditText.getText().toString();
                flow1 = flow1EditText.getText().toString();
                flow2 = flow2EditText.getText().toString();
                flow3 = flow3EditText.getText().toString();
                hour1 = startHourEditText.getText().toString();
                minute1 = startMinuteEditText.getText().toString();
                hour2 = endHourEditText.getText().toString();
                minute2 = endMinuteEditText.getText().toString();
                // Validate the login information
                // If valid, start MainActivity
                JSONObject data = new JSONObject();
                try {
                    data.put("cycle", cycle);
                    data.put("flow1", flow1);
                    data.put("flow2", flow2);
                    data.put("flow3", flow3);
                    data.put("startTime", hour1+":"+minute1);
                    data.put("stopTime", hour2+":"+minute2);
                    data.put("area", area);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (validateInputs(cycle, flow1, flow2, flow3, hour1, minute1, hour2, minute2)) {

                    Log.d("TEST", "JSON Object: " + data.toString());
//                sendDataMQTT(username + "feeds/command",data.toString());
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("jsonData", data.toString());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                }
            }
            private boolean validateInputs(String cycle, String flow1, String flow2, String flow3,
                                           String hour1, String minute1, String hour2, String minute2) {
                if (isEmpty(cycle) || isEmpty(flow1) || isEmpty(flow2) || isEmpty(flow3) ||
                        isEmpty(hour1) || isEmpty(minute1) || isEmpty(hour2) || isEmpty(minute2)) {
                    Toast.makeText(LightActivity.this, "Please fill in all fields.", Toast.LENGTH_SHORT).show();
                    return false;
                }
                if (!isNumeric(cycle) || !isInRange(cycle, 1, 100)) {
                    Toast.makeText(LightActivity.this, "Invalid cycle input. Must be a number between 1 and 10.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!isNumeric(flow1) || !isNumeric(flow2) || !isNumeric(flow3)) {
                    Toast.makeText(LightActivity.this, "All flow inputs must be numeric.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!isValidTime(hour1, minute1) || !isValidTime(hour2, minute2)) {
                    Toast.makeText(LightActivity.this, "Invalid time input. Hours must be 0-23 and minutes must be 0-59.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                if (!isEndTimeGreater(hour1, minute1, hour2, minute2)) {
                    Toast.makeText(LightActivity.this, "End time must be later than start time.", Toast.LENGTH_SHORT).show();
                    return false;
                }

                return true;
            }
            private boolean isEmpty(String str) {
                return str == null || str.trim().isEmpty();
            }
            private boolean isNumeric(String str) {
                try {
                    Double.parseDouble(str);
                    return true;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            private boolean isInRange(String str, int min, int max) {
                try {
                    int num = Integer.parseInt(str);
                    return num >= min && num <= max;
                } catch (NumberFormatException e) {
                    return false;
                }
            }

            private boolean isValidTime(String hour, String minute) {
                return isInRange(hour, 0, 23) && isInRange(minute, 0, 59);
            }

            private boolean isEndTimeGreater(String hour1, String minute1, String hour2, String minute2) {
                int startHour = Integer.parseInt(hour1);
                int startMinute = Integer.parseInt(minute1);
                int endHour = Integer.parseInt(hour2);
                int endMinute = Integer.parseInt(minute2);

                if (endHour > startHour) {
                    return true;
                } else if (endHour == startHour && endMinute > startMinute) {
                    return true;
                } else {
                    return false;
                }
            }
        });
    }

}
