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
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;

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

        // Set initial theme
        updateTheme();

        setVisible(true);
    }

    private JFreeChart createChart(CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createBarChart(
                "Distribution of Games by Release Year",
                "Release Year",
                "Number of Games",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // Set the same font size for both axis labels
        Font axisLabelFont = new Font("SansSerif", Font.PLAIN, 12);
        chart.getCategoryPlot().getDomainAxis().setLabelFont(axisLabelFont);
        chart.getCategoryPlot().getRangeAxis().setLabelFont(axisLabelFont);

        // Adjust the category label font size
        CategoryAxis categoryAxis = chart.getCategoryPlot().getDomainAxis();
        categoryAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 12)); // Same font size for category labels
        categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0)); // Rotate labels

        return chart;
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
            if (dataset.getRowCount() == 0) {
                JOptionPane.showMessageDialog(GameReleaseDataAnalysis.this, "No data to export.");
                return;
            }

            // Prompt user to select a file location to save the CSV
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            fileChooser.setSelectedFile(new File("release_year_data.csv")); // Default file name

            int userSelection = fileChooser.showSaveDialog(GameReleaseDataAnalysis.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                    // Write CSV header
                    writer.write("Release Year,Number of Games");
                    writer.newLine();

                    // Write data from the dataset
                    for (int i = 0; i < dataset.getColumnCount(); i++) {
                        String year = dataset.getColumnKey(i).toString();
                        Number count = dataset.getValue(0, i); // Assuming single series
                        writer.write(year + "," + count);
                        writer.newLine();
                    }

                    JOptionPane.showMessageDialog(GameReleaseDataAnalysis.this, "Data exported successfully to " + fileToSave.getAbsolutePath());
                } catch (IOException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(GameReleaseDataAnalysis.this, "Error saving file: " + ex.getMessage());
                }
            }
        }
    }

    private class ToggleThemeAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            isDarkMode = !isDarkMode;
            updateTheme();
        }
    }

    private void updateTheme() {
        if (isDarkMode) {
            getContentPane().setBackground(Color.DARK_GRAY);
            toggleThemeButton.setText("Switch to Light Mode");
            chartPanel.setBackground(Color.DARK_GRAY);
            chart.setBackgroundPaint(Color.DARK_GRAY);
            chart.getPlot().setBackgroundPaint(Color.DARK_GRAY);
            chart.getCategoryPlot().setDomainGridlinePaint(Color.LIGHT_GRAY);
            chart.getCategoryPlot().setRangeGridlinePaint(Color.LIGHT_GRAY);
            chart.getCategoryPlot().getRenderer().setSeriesPaint(0, Color.CYAN); // Change bar color
            
            // Change text colors in the chart
            chart.getCategoryPlot().getDomainAxis().setLabelPaint(Color.WHITE); // Axis label color
            chart.getCategoryPlot().getRangeAxis().setLabelPaint(Color.WHITE); // Axis label color
            chart.getCategoryPlot().getDomainAxis().setTickLabelPaint(Color.LIGHT_GRAY); // Tick label color
            chart.getCategoryPlot().getRangeAxis().setTickLabelPaint(Color.LIGHT_GRAY); // Tick label color
            chart.getLegend().setBackgroundPaint(Color.DARK_GRAY); // Legend background color
            chart.getLegend().setItemPaint(Color.WHITE); // Legend text color
            
            // Change button panel color
            JPanel buttonPanel = (JPanel) getContentPane().getComponent(1);
            buttonPanel.setBackground(Color.GRAY);
        } else {
            getContentPane().setBackground(Color.WHITE);
            toggleThemeButton.setText("Switch to Dark Mode");
            chartPanel.setBackground(Color.WHITE);
            chart.setBackgroundPaint(Color.WHITE);
            chart.getPlot().setBackgroundPaint(Color.WHITE);
            chart.getCategoryPlot().setDomainGridlinePaint(Color.GRAY);
            chart.getCategoryPlot().setRangeGridlinePaint(Color.GRAY);
            chart.getCategoryPlot().getRenderer().setSeriesPaint(0, Color.BLUE); // Change bar color
            
            // Change text colors in the chart
            chart.getCategoryPlot().getDomainAxis().setLabelPaint(Color.BLACK); // Axis label color
            chart.getCategoryPlot().getRangeAxis().setLabelPaint(Color.BLACK); // Axis label color
            chart.getCategoryPlot().getDomainAxis().setTickLabelPaint(Color.BLACK); // Tick label color
            chart.getCategoryPlot().getRangeAxis().setTickLabelPaint(Color.BLACK); // Tick label color
            chart.getLegend().setBackgroundPaint(Color.WHITE); // Legend background color
            chart.getLegend().setItemPaint(Color.BLACK); // Legend text color
            
            // Change button panel color
            JPanel buttonPanel = (JPanel) getContentPane().getComponent(1);
            buttonPanel.setBackground(Color.LIGHT_GRAY);
        }
        chartPanel.repaint(); // Refresh the chart panel
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameReleaseDataAnalysis::new);
    }
}
