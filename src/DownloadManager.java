import javax.swing.*;
import javax.swing.table.AbstractTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

public class DownloadManager {
    private static DownloadTableModel tableModel;
    private static final Map<Integer, DownloadController> activeDownloads = new HashMap<>();

    public static void main(String[] args) {
        JFrame frame = new JFrame("Download Manager");
        frame.setSize(800, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Top panel with URL input and download button
        JPanel panel = new JPanel();
        panel.setPreferredSize(new Dimension(0, 40));
        JTextField textField = new JTextField();
        textField.setPreferredSize(new Dimension(600, 30));
        JButton button = new JButton("Add Download");
        panel.add(textField);
        panel.add(button);
        frame.add(panel, BorderLayout.PAGE_START);

        // Main download list panel
        JPanel panel1 = new JPanel(new BorderLayout());
        panel1.setBorder(BorderFactory.createTitledBorder("Downloads"));

        tableModel = new DownloadTableModel();
        JTable table = new JTable(tableModel);
        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Object.class, new ProgressCellRenderer());

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setPreferredSize(new Dimension(780, 400));
        panel1.add(scrollPane, BorderLayout.CENTER);
        frame.add(panel1, BorderLayout.CENTER);

        // Bottom control buttons panel
        JPanel panel3 = new JPanel();
        panel3.setPreferredSize(new Dimension(0, 40));

        JButton pauseButton = new JButton("Pause");
        JButton resumeButton = new JButton("Resume");
        JButton cancelButton = new JButton("Cancel");
        JButton clearButton = new JButton("Clear");

        panel3.add(pauseButton);
        panel3.add(resumeButton);
        panel3.add(cancelButton);
        panel3.add(clearButton);
        frame.add(panel3, BorderLayout.PAGE_END);

        // Download button action
        button.addActionListener(e -> {
            String url = textField.getText();
            if (!url.isEmpty()) {
                downloadImage(url);
                textField.setText("");
            }
        });

        // Control buttons actions
        pauseButton.addActionListener(e -> pauseSelectedDownloads(table));
        resumeButton.addActionListener(e -> resumeSelectedDownloads(table));
        cancelButton.addActionListener(e -> cancelSelectedDownloads(table));
        clearButton.addActionListener(e -> clearCompletedDownloads());

        frame.setVisible(true);
    }

    private static void pauseSelectedDownloads(JTable table) {
        int[] selectedRows = table.getSelectedRows();
        for (int viewRow : selectedRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            DownloadController controller = activeDownloads.get(modelRow);
            if (controller != null) {
                controller.pauseDownload();
                tableModel.getDownloadAt(modelRow).status = "Paused";
            }
        }
        tableModel.fireTableDataChanged();
    }

    private static void resumeSelectedDownloads(JTable table) {
        int[] selectedRows = table.getSelectedRows();
        for (int viewRow : selectedRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            DownloadController controller = activeDownloads.get(modelRow);
            if (controller != null) {
                controller.resumeDownload();
                tableModel.getDownloadAt(modelRow).status = "Downloading";
            }
        }
        tableModel.fireTableDataChanged();
    }

    private static void cancelSelectedDownloads(JTable table) {
        int[] selectedRows = table.getSelectedRows();
        for (int viewRow : selectedRows) {
            int modelRow = table.convertRowIndexToModel(viewRow);
            DownloadController controller = activeDownloads.remove(modelRow);
            if (controller != null) {
                controller.cancelDownload();
                DownloadItem item = tableModel.getDownloadAt(modelRow);
                item.status = "Canceled";
                new File(item.fileName).delete(); // Delete partial file
            }
        }
        tableModel.fireTableDataChanged();
    }

    private static void clearCompletedDownloads() {
        List<DownloadItem> toRemove = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            DownloadItem item = tableModel.getDownloadAt(i);
            if (item.status.equals("Completed") || item.status.equals("Canceled") || item.status.startsWith("Error")) {
                toRemove.add(item);
                activeDownloads.remove(i);
            }
        }
        tableModel.removeDownloads(toRemove);
    }

    public static void downloadImage(String urlString) {
        DownloadItem item = new DownloadItem();
        item.url = urlString;
        item.status = "Connecting...";
        item.timestamp = System.currentTimeMillis();

        SwingUtilities.invokeLater(() -> {
            int row = tableModel.addDownload(item);
            activeDownloads.put(row, new DownloadController());
        });

        new Thread(() -> {
            DownloadController controller = null;
            try {
                URL url = new URL(urlString);
                item.fileName = getImageFileName(url);
                item.status = "Downloading";

                // Get controller for this download
                controller = getControllerForDownload(item);
                if (controller == null) return;

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();

                // Check for existing partial download
                File outputFile = new File(item.fileName);
                if (outputFile.exists() && !controller.isFreshDownload()) {
                    item.downloaded = outputFile.length();
                    connection.setRequestProperty("Range", "bytes=" + item.downloaded + "-");
                }

                connection.connect();
                item.totalSize = connection.getContentLength() + item.downloaded;

                try (BufferedInputStream in = new BufferedInputStream(connection.getInputStream());
                     FileOutputStream fileOutputStream = new FileOutputStream(outputFile, true)) {

                    byte[] dataBuffer = new byte[1024];
                    int bytesRead;

                    while ((bytesRead = in.read(dataBuffer, 0, 1024)) != -1) {
                        if (controller.isPaused()) {
                            while (controller.isPaused() && !controller.isCanceled()) {
                                Thread.sleep(500);
                            }
                        }

                        if (controller.isCanceled()) {
                            item.status = "Canceled";
                            break;
                        }

                        fileOutputStream.write(dataBuffer, 0, bytesRead);
                        item.downloaded += bytesRead;

                        // Calculate progress percentage
                        if (item.totalSize > 0) {
                            item.progress = (int) ((item.downloaded * 100) / item.totalSize);
                        }

                        // Update UI periodically
                        if (item.downloaded % 65536 == 0) { // Update every 64KB
                            SwingUtilities.invokeLater(() -> {
                                tableModel.fireTableDataChanged();
                            });
                        }
                    }

                    if (!controller.isCanceled()) {
                        item.status = "Completed";
                    }
                }
            } catch (Exception e) {
                item.status = "Error: " + e.getMessage();
            } finally {
                if (controller != null && controller.isCanceled()) {
                    new File(item.fileName).delete();
                }
                SwingUtilities.invokeLater(() -> {
                    tableModel.fireTableDataChanged();
                });
            }
        }).start();
    }

    private static DownloadController getControllerForDownload(DownloadItem item) {
        for (Map.Entry<Integer, DownloadController> entry : activeDownloads.entrySet()) {
            if (tableModel.getDownloadAt(entry.getKey()) == item) {
                return entry.getValue();
            }
        }
        return null;
    }

    private static String getImageFileName(URL url) {
        String path = url.getPath();
        String fileName = Paths.get(path).getFileName().toString();

        if (!fileName.matches("(?i).*\\.(jpg|jpeg|png|gif|bmp|webp)$")) {
            try {
                String contentType = url.openConnection().getContentType();
                if (contentType != null) {
                    if (contentType.contains("jpeg")) fileName += ".jpg";
                    else if (contentType.contains("png")) fileName += ".png";
                    else if (contentType.contains("gif")) fileName += ".gif";
                    else if (contentType.contains("bmp")) fileName += ".bmp";
                    else if (contentType.contains("webp")) fileName += ".webp";
                    else fileName += ".jpg";
                }
            } catch (Exception e) {
                fileName += ".jpg";
            }
        }

        // Ensure unique filename
        int counter = 1;
        String originalName = fileName;
        while (new File(fileName).exists()) {
            int dotIndex = originalName.lastIndexOf('.');
            if (dotIndex > 0) {
                fileName = originalName.substring(0, dotIndex) + " (" + counter + ")" + originalName.substring(dotIndex);
            } else {
                fileName = originalName + " (" + counter + ")";
            }
            counter++;
        }
        return fileName;
    }

    static class DownloadItem {
        String url;
        String fileName;
        String status;
        long downloaded;
        long totalSize;
        int progress;
        long timestamp;
    }

    static class DownloadController {
        private final AtomicBoolean paused = new AtomicBoolean(false);
        private final AtomicBoolean canceled = new AtomicBoolean(false);
        private final AtomicBoolean freshDownload = new AtomicBoolean(true);

        public void pauseDownload() {
            paused.set(true);
        }

        public void resumeDownload() {
            paused.set(false);
            freshDownload.set(false);
        }

        public void cancelDownload() {
            canceled.set(true);
        }

        public boolean isPaused() {
            return paused.get();
        }

        public boolean isCanceled() {
            return canceled.get();
        }

        public boolean isFreshDownload() {
            return freshDownload.get();
        }
    }

    static class DownloadTableModel extends AbstractTableModel {
        private final String[] columnNames = {"Filename", "Size", "Progress", "Status"};
        private final List<DownloadItem> downloads = new ArrayList<>();

        public int addDownload(DownloadItem item) {
            downloads.add(item);
            fireTableRowsInserted(downloads.size()-1, downloads.size()-1);
            return downloads.size()-1;
        }

        public DownloadItem getDownloadAt(int row) {
            return downloads.get(row);
        }

        public void removeDownloads(List<DownloadItem> items) {
            downloads.removeAll(items);
            fireTableDataChanged();
        }

        @Override public int getRowCount() { return downloads.size(); }
        @Override public int getColumnCount() { return columnNames.length; }
        @Override public String getColumnName(int column) { return columnNames[column]; }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            DownloadItem item = downloads.get(rowIndex);
            switch (columnIndex) {
                case 0: return item.fileName != null ? item.fileName : "Processing...";
                case 1: return formatFileSize(item.totalSize);
                case 2: return item.progress;
                case 3: return item.status;
                default: return "";
            }
        }

        private String formatFileSize(long size) {
            if (size <= 0) return "Unknown";
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return (size / 1024) + " KB";
            return (size / (1024 * 1024)) + " MB";
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return columnIndex == 2 ? Integer.class : String.class;
        }
    }

    static class ProgressCellRenderer extends javax.swing.table.DefaultTableCellRenderer {
        private final JProgressBar progressBar = new JProgressBar(0, 100);

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            if (column == 2) {
                int progress = (Integer) value;
                progressBar.setValue(progress);
                progressBar.setString(progress + "%");
                progressBar.setStringPainted(true);

                // Color coding based on status
                DownloadItem item = ((DownloadTableModel)table.getModel()).getDownloadAt(
                        table.convertRowIndexToModel(row));

                if (item.status.equals("Completed")) {
                    progressBar.setForeground(new Color(0, 100, 0)); // Dark green
                } else if (item.status.equals("Paused")) {
                    progressBar.setForeground(Color.ORANGE);
                } else if (item.status.startsWith("Error") || item.status.equals("Canceled")) {
                    progressBar.setForeground(Color.RED);
                } else {
                    progressBar.setForeground(new Color(0, 0, 200)); // Blue
                }

                return progressBar;
            }
            return super.getTableCellRendererComponent(table, value, isSelected,
                    hasFocus, row, column);
        }
    }
}