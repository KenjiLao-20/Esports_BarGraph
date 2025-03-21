import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.jfree.data.UnknownKeyException;

public class GameReleaseDataAnalysis extends JFrame {
    private DefaultCategoryDataset dataset;
    private JFreeChart chart;
    private ChartPanel chartPanel;
    private JButton uploadButton;
    private JButton processButton;
    private JButton exportButton;
    private JButton toggleThemeButton;
    private File selectedFile; // Store the uploaded file
    private boolean isDarkMode = false;

    public GameReleaseDataAnalysis() {
        setTitle("Game Release Year Analysis");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        dataset = new DefaultCategoryDataset();
        chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        add(chartPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        uploadButton = new JButton("Upload CSV");
        processButton = new JButton("Process Data");
        exportButton = new JButton("Export Data");
        exportButton.setEnabled(false); // Initially disabled
        toggleThemeButton = new JButton("Switch to Dark Mode");

        buttonPanel.add(uploadButton);
        buttonPanel.add(processButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(toggleThemeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        uploadButton.addActionListener(new UploadFileAction());
        processButton.addActionListener(new ProcessDataAction());
        exportButton.addActionListener(new ExportDataAction());
        toggleThemeButton.addActionListener(new ToggleThemeAction());

        setVisible(true);
    }

    private JFreeChart createChart(CategoryDataset dataset) {
        return ChartFactory.createBarChart(
                "Distribution of Games by Release Year",
                "Release Year",
                "Number of Games",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);
    }

    private class UploadFileAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                selectedFile = fileChooser.getSelectedFile();
                JOptionPane.showMessageDialog(GameReleaseDataAnalysis.this, "File uploaded: " + selectedFile.getName());
                exportButton.setEnabled(true); // Enable export button after uploading
            }
        }
    }

    private class ProcessDataAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedFile != null) {
                processCSVFile(selectedFile);
            } else {
                JOptionPane.showMessageDialog(GameReleaseDataAnalysis.this, "Please upload a CSV file first.");
            }
        }
    }

private void processCSVFile(File file) {
    dataset.clear(); // Clear previous data
    Map<String, Integer> yearCountMap = new TreeMap<>(); // Use TreeMap to store year counts

    try (BufferedReader br = new BufferedReader(new FileReader(file))) {
        String line;
        br.readLine(); // Skip header
        while ((line = br.readLine()) != null) {
            String[] columns = line.split(","); // Split by comma
            if (columns.length > 8) {
                String releaseYear = columns[8].trim(); // Assuming the release year is in the 9th column
                if (!releaseYear.isEmpty()) {
                    // Check if the year is within the desired range
                    int year;
                    try {
                        year = Integer.parseInt(releaseYear);
                        if (year >= 1993 && year <= 2020) {
                            // Increment the count for the year
                            yearCountMap.put(releaseYear, yearCountMap.getOrDefault(releaseYear, 0) + 1);
                        }
                    } catch (NumberFormatException e) {
                        // Handle the case where the year is not a valid integer
                        System.out.println("Invalid year format: " + releaseYear);
                    }
                }
            }
        }

        // Add the sorted data to the dataset
        for (Map.Entry<String, Integer> entry : yearCountMap.entrySet()) {
            dataset.addValue(entry.getValue(), "Games", entry.getKey());
        }

        chart.fireChartChanged(); // Refresh the chart
        chartPanel.revalidate(); // Revalidate the chart panel
        chartPanel.repaint(); // Repaint the chart panel
    } catch (IOException ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
    }
}

    private class ExportDataAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            // Implement export functionality here (e.g., save dataset to CSV)
            JOptionPane.showMessageDialog(GameReleaseDataAnalysis.this, "Export functionality not implemented.");
        }
    }

    private class ToggleThemeAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            isDarkMode = !isDarkMode;
            if (isDarkMode) {
                getContentPane().setBackground(Color.DARK_GRAY);
                toggleThemeButton.setText("Switch to Light Mode");
                chartPanel.setBackground(Color.DARK_GRAY);
            } else {
                getContentPane().setBackground(Color.WHITE);
                toggleThemeButton.setText("Switch to Dark Mode");
                chartPanel.setBackground(Color.WHITE);
            }
            chartPanel.repaint(); // Refresh the chart panel
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameReleaseDataAnalysis::new);
    }
}
