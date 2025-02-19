package com.example.appmqtt;

import java.io.Serializable;

public class InfoHistory implements Serializable {
    private String info;
    private String time;
    private long timestamp;  // Thêm trường này

    public InfoHistory(String info, String time) {
        this.info = info;
        this.time = time;
        this.timestamp = System.currentTimeMillis();  // Lưu timestamp khi tạo đối tượng
    }

    public String getInfo() { return info; }
    public String getTime() { return time; }
    public long getTimestamp() { return timestamp; }
}