package com.example.appmqtt;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class SearchHistoryActivity extends AppCompatActivity {
    EditText searchEditText;
    Button searchButton;
    TextView searchResults, searchResults1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search_history);

        searchEditText = findViewById(R.id.searchEditText);
        searchButton = findViewById(R.id.searchButton);
        searchResults = findViewById(R.id.searchResults);
        searchResults1 = findViewById(R.id.searchResults1);

        searchButton.setOnClickListener(v -> {
            String keyword = searchEditText.getText().toString().trim();
            if (!keyword.isEmpty()) {
                displaySearchResults(keyword);
            } else {
                Toast.makeText(this, "Vui lòng nhập từ khóa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private enum FileType {
        ACTION,
        INFO,
        UNKNOWN
    }

    private FileType getFileType(String fileName) {
        if (fileName.toLowerCase().contains("actionhistory")) {
            return FileType.ACTION;
        } else if (fileName.toLowerCase().contains("infohistory")) {
            return FileType.INFO;
        } else {
            return FileType.UNKNOWN;
        }
    }

    private void displaySearchResults(String keyword) {
        StringBuilder actionResults = new StringBuilder();
        StringBuilder infoResults = new StringBuilder();
        File directory = getExternalFilesDir(null);
        File[] files = directory != null ? directory.listFiles() : null;

        if (files != null && files.length > 0) {
            for (File file : files) {
                if (file.isFile() && file.getName().endsWith(".txt")) {
                    FileType fileType = getFileType(file.getName());  // Phân loại file

                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        String line;
                        boolean fileHasResults = false;
                        StringBuilder fileContent = new StringBuilder();

                        // Đọc toàn bộ nội dung file
                        while ((line = reader.readLine()) != null) {
                            fileContent.append(line).append("\n");

                            // Tìm kiếm từ khóa trong nội dung file
                            if (line.toLowerCase().contains(keyword.toLowerCase())) {
                                if (!fileHasResults) {
                                    // Phân loại theo loại file và hiển thị kết quả
                                    if (fileType == FileType.ACTION) {
                                        actionResults.append("Kết quả trong file ACTION: ").append(file.getName()).append("\n");
                                    } else if (fileType == FileType.INFO) {
                                        infoResults.append("Kết quả trong file INFO: ").append(file.getName()).append("\n");
                                    }
                                    fileHasResults = true;
                                }
                            }
                        }

                        // Nếu file có chứa từ khóa, in toàn bộ nội dung file
                        if (fileHasResults) {
                            if (fileType == FileType.ACTION) {
                                actionResults.append("\nToàn bộ nội dung file:\n").append(fileContent).append("\n");
                                actionResults.append("------------------------------------------------------------\n");
                            } else if (fileType == FileType.INFO) {
                                infoResults.append("\nToàn bộ nội dung file:\n").append(fileContent).append("\n");
                                infoResults.append("------------------------------------------------------------\n");
                            }
                        }

                    } catch (IOException e) {
                        Toast.makeText(this, "Lỗi đọc file " + file.getName() + ": " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            // Hiển thị kết quả tìm kiếm theo từng loại file
            if (actionResults.length() > 0) {
                searchResults.setText(actionResults.toString());
            } else {
                searchResults.setText("Không tìm thấy kết quả thao tác cho từ khóa \"" + keyword + "\".");
            }

            if (infoResults.length() > 0) {
                searchResults1.setText(infoResults.toString());
            } else {
                searchResults1.setText("Không tìm thấy kết quả dữ liệu cho từ khóa \"" + keyword + "\".");
            }
        } else {
            Toast.makeText(this, "Không có file lịch sử nào.", Toast.LENGTH_SHORT).show();
        }
    }
}
