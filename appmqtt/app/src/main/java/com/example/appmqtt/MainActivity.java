package com.example.appmqtt;

import static android.content.ContentValues.TAG;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    TextView nhietdo, doam;
    ImageView quat,den,hutam,phunsuong;
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    Switch swden, swquat,swdehumi,swhumidifier;
    Button btnExport;
    ImageButton btnchucnang;

    List<ActionHistory> actionHistoryList;
    List<InfoHistory>   InfoHistoryList;

    private static final double TEMP1 = 33;
    private static final double TEMP2 = 16;
    private static final double HUMIDITY1 = 70;
    private static final double HUMIDITY2 = 55;

    boolean flag = true;
    private boolean AutoMode = false;

    boolean tempHighActivated = false;
    boolean tempLowActivated = false;
    boolean tempNormalActivated = false;

    boolean humidityHighActivated = false;
    boolean humidityLowActivated = false;
    boolean humidityNormalActivated = false;

    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        quat = findViewById(R.id.fan);
        den = findViewById(R.id.light);
        hutam = findViewById(R.id.hutam);
        phunsuong = findViewById(R.id.phunsuong);
        swden = findViewById(R.id.swden);
        swquat = findViewById(R.id.swquat);
        swdehumi = findViewById(R.id.swdehumi);
        swhumidifier = findViewById(R.id.swhumidifier);
        nhietdo = findViewById(R.id.TvTemp);
        doam = findViewById(R.id.TvHumi);
        btnExport = findViewById(R.id.btnExport);
        btnchucnang = findViewById(R.id.btnchucnang);
        MQTT();

        actionHistoryList = new ArrayList<>();
        InfoHistoryList = new ArrayList<>();

        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                exportHistoryToFile();
                exportInfoHistoryToFile();
                Intent intent = new Intent(MainActivity.this, SearchHistoryActivity.class);
                intent.putExtra("actionHistoryList", new ArrayList<>(actionHistoryList)); // Chuyển danh sách
                intent.putExtra("InfoHistoryList",new ArrayList<>(InfoHistoryList));
                startActivity(intent);
            }
        });
        btnchucnang.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (flag) {  // Khi flag là true, chuyển sang chế độ tự động
                    AutoMode = true;
                    Toast.makeText(MainActivity.this, "Chuyển sang chế độ Thao tác tự động", Toast.LENGTH_SHORT).show();
                    btnchucnang.setImageResource(R.drawable.automatic);
                    hashCode();
                } else {  // Khi flag là false, chuyển sang chế độ thủ công
                    AutoMode = false;
                    Toast.makeText(MainActivity.this, "Chuyển sang chế độ Thao tác thủ công", Toast.LENGTH_SHORT).show();
                    btnchucnang.setImageResource(R.drawable.automaticoff);
                    swden.setChecked(false);
                    swquat.setChecked(false);
                    swdehumi.setChecked(false);
                    swhumidifier.setChecked(false);
                    quat.setImageResource(R.drawable.quatoff1);
                    den.setImageResource(R.drawable.lightoff1);
                    hutam.setImageResource(R.drawable.dehumioff);
                    phunsuong.setImageResource(R.drawable.humidifier);

                }
                flag = !flag;  // Đảo giá trị của flag
            }
        });

    }

    public void MQTT() {
        String clientId = MqttClient.generateClientId();
        final MqttAndroidClient client = new MqttAndroidClient(this.getApplicationContext(),"tcp://mqtt-dashboard.com:1883",clientId);
        MqttConnectOptions options = new MqttConnectOptions();
        options.setMqttVersion(MqttConnectOptions.MQTT_VERSION_3_1);
        options.setCleanSession(true);
        options.setUserName("doann7");
        options.setPassword("doannhom7".toCharArray());

        try {
            final IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    SUB(client,"temperature/swquat");
                    SUB(client,"temperature/swden");
                    SUB(client,"temperature/nhietdo");
                    SUB(client,"humidity/doam");
                    SUB(client,"humidity/swhutam");
                    SUB(client,"humidity/swphunsuong");



                    client.setCallback(new MqttCallback() {
                        @Override
                        public void connectionLost(Throwable cause) {
                            Toast.makeText(MainActivity.this, "mat ket noi sever", Toast.LENGTH_SHORT).show();
                        }
                        @Override
                        public void messageArrived(String topic, MqttMessage message) throws Exception {
                            Log.d("MQTT", "Message arrived. Topic: " + topic + ". Message: " + message.toString());
                            //den
                            if (topic.equals("temperature/swden")) {
                                if (message.toString().equals("ON")) {
                                    swden.setChecked(true);
                                    recordAction("Đèn: Bật");
                                    Toast.makeText(MainActivity.this,"Bật đèn",Toast.LENGTH_SHORT).show();
                                    den.setImageResource(R.drawable.lighton1);
                                } else {
                                    swden.setChecked(false);
                                    recordAction("Đèn: Tắt");
                                    Toast.makeText(MainActivity.this,"Tắt đèn",Toast.LENGTH_SHORT).show();
                                    den.setImageResource(R.drawable.lightoff1);
                                }
                            }
                            //quat
                            if (topic.equals("temperature/swquat")) {
                                if (message.toString().equals("ON")) {
                                    swquat.setChecked(true);
                                    recordAction("Quạt: Bật");
                                    Toast.makeText(MainActivity.this,"Bật quạt",Toast.LENGTH_SHORT).show();
                                    quat.setImageResource(R.drawable.quat1);
                                } else {
                                    swquat.setChecked(false);
                                    recordAction("Quạt: Tắt");
                                    Toast.makeText(MainActivity.this,"Tắt quạt",Toast.LENGTH_SHORT).show();
                                    quat.setImageResource(R.drawable.quatoff1);
                                }
                            }
                            //hut am
                            if (topic.equals("humidity/swhutam")) {
                                if (message.toString().equals("ON")) {
                                    swdehumi.setChecked(true);
                                    recordAction("Máy hút ẩm: Bật");
                                    Toast.makeText(MainActivity.this,"Bật máy hút ẩm",Toast.LENGTH_SHORT).show();
                                    hutam.setImageResource(R.drawable.dehumion);
                                } else {
                                    swdehumi.setChecked(false);
                                    recordAction("Máy hút ẩm: Tắt");
                                    Toast.makeText(MainActivity.this,"Tắt máy hút ẩm",Toast.LENGTH_SHORT).show();
                                    hutam.setImageResource(R.drawable.dehumioff);
                                }
                            }
                            //phun suong
                            if (topic.equals("humidity/swphunsuong")) {
                                if (message.toString().equals("ON")) {
                                    swhumidifier.setChecked(true);
                                    recordAction("Máy phun sương: Bật");
                                    Toast.makeText(MainActivity.this,"Bật máy phun sương",Toast.LENGTH_SHORT).show();
                                    phunsuong.setImageResource(R.drawable.humidifieron);
                                } else {
                                    swhumidifier.setChecked(false);
                                    recordAction("Máy phun sương: Tắt");
                                    Toast.makeText(MainActivity.this,"Tắt máy phun sương",Toast.LENGTH_SHORT).show();
                                    phunsuong.setImageResource(R.drawable.humidifier);
                                }
                            }

                            //nhiet do
                            if(topic.equals("temperature/nhietdo")) {
                                Log.d("MQTT", "Nhiệt độ nhận được: " + message.toString());
                                nhietdo.setText(message.toString() + "\u2103" );
                                double currentTemp = Double.parseDouble(message.toString());
                                recordInfo("Nhiệt độ: " + message.toString() + "\u2103");

                                if (AutoMode) {
                                    if (currentTemp >= TEMP1  && !tempHighActivated) {
                                        swquat.setChecked(true);
                                        swden.setChecked(false);
                                        publishMessage(client, "temperature/swquat", "ON");
                                        publishMessage(client, "temperature/swden", "OFF");
                                        recordAction("Quạt tự động bật do nhiệt độ cao");
                                        quat.setImageResource(R.drawable.quat1);
                                        den.setImageResource(R.drawable.lightoff1);

                                        tempHighActivated = true;
                                        tempLowActivated = false;
                                        tempNormalActivated = false;

                                    } else if (currentTemp < TEMP2 && !tempLowActivated) {
                                        swden.setChecked(true);
                                        swquat.setChecked(false);
                                        publishMessage(client, "temperature/swden", "ON");
                                        publishMessage(client, "temperature/swquat", "OFF");
                                        recordAction("Đèn tự động bật do nhiệt độ cao");
                                        den.setImageResource(R.drawable.lighton1);
                                        quat.setImageResource(R.drawable.quatoff1);

                                        tempLowActivated = true;
                                        tempHighActivated = false;
                                        tempNormalActivated = false;

                                    } else if (currentTemp < TEMP1 && currentTemp >= TEMP2 && !tempNormalActivated){
                                        swden.setChecked(false);
                                        swquat.setChecked(false);
                                        publishMessage(client, "temperature/swquat", "OFF");
                                        publishMessage(client, "temperature/den", "OFF");
                                        quat.setImageResource(R.drawable.quatoff1);
                                        den.setImageResource(R.drawable.lightoff1);;

                                        tempNormalActivated = true;
                                        tempHighActivated = false;
                                        tempLowActivated = false;
                                    }
                                } else if(!AutoMode){
                                    if (tempHighActivated || tempLowActivated || tempNormalActivated) {
                                        publishMessage(client, "temperature/swquat", "OFF");
                                        publishMessage(client, "temperature/den", "OFF");
                                        tempHighActivated = false;
                                        tempLowActivated = false;
                                        tempNormalActivated = false;
                                    }
                                }
                            }
                            //do am
                            if(topic.equals("humidity/doam")){
                                Log.d("MQTT", "Độ ẩm nhận được: " + message.toString());
                                doam.setText(message.toString() + "%");
                                double currentHumidity = Double.parseDouble(message.toString());
                                recordInfo("Độ ẩm: "+ message.toString()+ "%");

                                if (AutoMode) {
                                    if (currentHumidity >= HUMIDITY1 && !humidityHighActivated) {
                                        swdehumi.setChecked(true);
                                        swhumidifier.setChecked(false);
                                        publishMessage(client, "humidity/swhutam", "ON");
                                        publishMessage(client, "humidity/swphunsuong", "OFF");
                                        recordAction("Máy hút ẩm tự động bật do độ ẩm cao");;
                                        hutam.setImageResource(R.drawable.dehumion);
                                        phunsuong.setImageResource(R.drawable.humidifier);

                                        humidityHighActivated = true;
                                        humidityLowActivated = false;
                                        humidityNormalActivated = false;

                                    } else if (currentHumidity < HUMIDITY2 && !humidityLowActivated) {
                                        swhumidifier.setChecked(true);
                                        swdehumi.setChecked(false);
                                        publishMessage(client, "humidity/swphunsuong", "ON");
                                        publishMessage(client, "humidity/swhutam", "OFF");
                                        recordAction("Máy phun sương tự động bật do độ ẩm cao");
                                        phunsuong.setImageResource(R.drawable.humidifieron);
                                        hutam.setImageResource(R.drawable.dehumioff);
                                        humidityLowActivated = true;
                                        humidityHighActivated = false;
                                        humidityNormalActivated = false;

                                    } else if (currentHumidity < HUMIDITY1 && currentHumidity >= HUMIDITY2 && !humidityNormalActivated){
                                        swdehumi.setChecked(false);
                                        swhumidifier.setChecked(false);
                                        publishMessage(client, "humidity/swhutam", "OFF");
                                        publishMessage(client, "humidity/swphunsuong", "OFF");
                                        hutam.setImageResource(R.drawable.dehumioff);
                                        phunsuong.setImageResource(R.drawable.humidifier);

                                        humidityNormalActivated = true;
                                        humidityHighActivated = false;
                                        humidityLowActivated = false;
                                    }
                                } else if (!AutoMode){
                                    if (humidityHighActivated || humidityLowActivated || humidityNormalActivated) {
                                        publishMessage(client, "humidity/swhutam", "OFF");
                                        publishMessage(client, "humidity/swphunsuong", "OFF");

                                        humidityHighActivated = false;
                                        humidityLowActivated = false;
                                        humidityNormalActivated = false;
                                    }

                                }

                            }
                        }
                        @Override
                        public void deliveryComplete(IMqttDeliveryToken token) {
                        }
                    });

                    swden.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String topic = "temperature/swden";
                            String payload = swden.isChecked() ? "ON" : "OFF"; // Kiểm tra trạng thái công tắc
                            publishMessage(client, topic, payload);
                        }
                    });
                    swquat.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String topic = "temperature/swquat";
                            String payload = swquat.isChecked() ? "ON" : "OFF"; // Kiểm tra trạng thái công tắc
                            publishMessage(client, topic, payload);
                        }
                    });
                    swdehumi.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String topic = "humidity/swhutam";
                            String payload = swdehumi.isChecked() ? "ON" : "OFF"; // Kiểm tra trạng thái công tắc
                            publishMessage(client, topic, payload);
                        }
                    });
                    swhumidifier.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String topic = "humidity/swphunsuong";
                            String payload = swhumidifier.isChecked() ? "ON" : "OFF"; // Kiểm tra trạng thái công tắc
                            publishMessage(client, topic, payload);
                        }
                    });
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Toast.makeText(MainActivity.this, "failed", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }//end mqtt
    public void SUB(MqttAndroidClient client, String topic) {
        int qos = 1; // Mức độ Chất lượng dịch vụ
        try {
            IMqttToken subToken = client.subscribe(topic, qos); // Truyền cả topic và qos
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    Log.d("MQTT", "Subscribed to topic: " + topic);
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    Log.e("MQTT", "Failed to subscribe to topic: " + topic, exception);
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void publishMessage(MqttAndroidClient client, String topic, String payload) {
        try {
            byte[] encodePayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodePayload);
            message.setRetained(true);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }
    private void recordAction(String action) {
        long currentTime = System.currentTimeMillis();
        String timeString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(currentTime));
        actionHistoryList.add(new ActionHistory(action, timeString));
    }
    private void recordInfo(String info) {
        long currentTime = System.currentTimeMillis();
        String timeString = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(new Date(currentTime));
        InfoHistoryList.add(new InfoHistory(info, timeString));
    }
    private void exportHistoryToFile() {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        File file = new File(getExternalFilesDir(null), "ActionHistory_" + currentDate + ".txt");
        try (FileWriter writer = new FileWriter(file, true)) {
            for (ActionHistory actionHistory : actionHistoryList) {
                if (actionHistory.getTime().startsWith(currentDate.replace("-", "/"))) {
                    writer.write(actionHistory.getAction() + " - " + actionHistory.getTime() + "\n");
                }
            }
            writer.flush();
            Toast.makeText(this, "Lịch sử đã được xuất" , Toast.LENGTH_LONG).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Đã xảy ra lỗi khi ghi file: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }
    private void exportInfoHistoryToFile() {
        String currentDate = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
        File file = new File(getExternalFilesDir(null), "InfoHistory_" + currentDate + ".txt");
        String currentDateForContent = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(new Date());


        double maxTemperature = Double.MIN_VALUE;
        double minTemperature = Double.MAX_VALUE;
        double maxHumidity = Double.MIN_VALUE;
        double minHumidity = Double.MAX_VALUE;

        long currentTime = System.currentTimeMillis();
        long fifteenMinutesAgo = currentTime - (2 * 60 * 1000); // 15 phút trước

        // Tìm giá trị cao nhất và thấp nhất trong 15 phút gần nhất
        for (InfoHistory infoHistory : InfoHistoryList) {
            if (infoHistory.getTimestamp() >= fifteenMinutesAgo && infoHistory.getTimestamp() <= currentTime) {
                String info = infoHistory.getInfo();
                try {
                    if (info.startsWith("Nhiệt độ:")) {
                        String tempStr = info.split(":")[1].trim().replace("\u2103", "");
                        double temp = Double.parseDouble(tempStr);
                        maxTemperature = Math.max(maxTemperature, temp);
                        minTemperature = Math.min(minTemperature, temp);
                    } else if (info.startsWith("Độ ẩm:")) {
                        String humStr = info.split(":")[1].trim().replace("%", "");
                        double humidity = Double.parseDouble(humStr);
                        maxHumidity = Math.max(maxHumidity, humidity);
                        minHumidity = Math.min(minHumidity, humidity);
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        try (FileWriter writer = new FileWriter(file, true)) {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
            String currentTimeString = sdf.format(new Date(currentTime));
            String fifteenMinutesAgoString = sdf.format(new Date(fifteenMinutesAgo));

            boolean hasTemperatureData = maxTemperature != Double.MIN_VALUE || minTemperature != Double.MIN_VALUE;
            boolean hasHumidityData = maxHumidity != Double.MIN_VALUE || minHumidity != Double.MIN_VALUE;

            if (hasTemperatureData || hasHumidityData) {
                writer.write("***Thống kê từ " + fifteenMinutesAgoString + " đến " + currentTimeString + " ngày " + currentDateForContent + ":\n");

                // Ghi dữ liệu nhiệt độ nếu có
                if (hasTemperatureData) {
                    if (maxTemperature != Double.MIN_VALUE) {
                        writer.write(String.format(Locale.getDefault(), "Nhiệt độ cao nhất: %.1f\u2103 "+ "\n", maxTemperature));
                    }
                    if (minTemperature != Double.MIN_VALUE) {
                        writer.write(String.format(Locale.getDefault(), "Nhiệt độ thấp nhất: %.1f\u2103 " + "\n", minTemperature));
                    }
                }

                // Ghi dữ liệu độ ẩm nếu có
                if (hasHumidityData) {
                    if (maxHumidity != Double.MIN_VALUE) {
                        writer.write(String.format(Locale.getDefault(), "Độ ẩm cao nhất: %.1f%% " + "\n", maxHumidity));
                    }
                    if (minHumidity != Double.MIN_VALUE) {
                        writer.write(String.format(Locale.getDefault(), "Độ ẩm thấp nhất: %.1f%% " + "\n", minHumidity));
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}

