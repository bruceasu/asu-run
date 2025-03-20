package me.asu.run.ui;

import java.util.Collections;
import java.util.List;
import javax.swing.DefaultListModel;
import lombok.Data;
import me.asu.run.FileInfo;

@Data
public class Words {

    DefaultListModel<String> model         = new DefaultListModel<>();
    int                      selectedIndex = -1;
    List<FileInfo>           data;
    int                      page          = 1;
    int                      pageSize      = 10;
    int                      totalPages    = 0;

    public void setSelectedIndex(int selectedIndex) {
        this.selectedIndex = selectedIndex + (page - 1) * pageSize;
    }

    public Words(List<FileInfo> data) {
        setData(data);
    }

    public void setData(List<FileInfo> data) {
        if (data == null) { data = Collections.emptyList(); }
        this.data  = data;
        this.page = 1;
        this.totalPages = (data.size() + pageSize - 1) / pageSize;
    }

    public DefaultListModel<String> getModel(int page) {
        if (page < 1) { page = 1; }
        if (page > totalPages && totalPages > 0) { page = totalPages; }
        this.page = page;
        int start = (page - 1) * pageSize;
        int end = page * pageSize;
        if (data != null) {
            model.clear();
            for (int i = 0, j = start; j < data.size() && j < end; i++, j++) {
                String s = data.get(j).getName() + " | " + data.get(j).getPath();
                model.addElement(i + ".  " + s);
            }
        }
        selectedIndex = 0;
        return model;
    }

    public FileInfo getSelected() {
        if (data == null || data.isEmpty()) {
            return null;
        }
        if (selectedIndex < -1) {
            selectedIndex = 0;
        } else if (selectedIndex >= data.size()) {
            selectedIndex = data.size() - 1;
        }
        return data.get(selectedIndex + (page - 1) * pageSize);
    }
}