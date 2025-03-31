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
import org.jfree.chart.renderer.category.BarRenderer;

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
    private Map<String, Integer> yearCountMap; // Store the final values
    private Timer animationTimer;
    private int animationStep = 0;
    
    // Animation settings
    private final int TOTAL_ANIMATION_STEPS = 160; // Number of steps for animation (8 seconds at 50 ms per step)
    private final int ANIMATION_DURATION_MS = 8000; // Total animation duration in milliseconds
    private final int TIMER_DELAY_MS = ANIMATION_DURATION_MS / TOTAL_ANIMATION_STEPS; // Calculate delay per step

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
        categoryAxis.setLabelFont(new Font("SansSerif", Font.PLAIN, 12));
        categoryAxis.setCategoryLabelPositions(CategoryLabelPositions.createUpRotationLabelPositions(Math.PI / 4.0));

        // Set up custom bar renderer
        BarRenderer renderer = new BarRenderer() {
            @Override
            public Paint getItemPaint(int row, int column) {
                // This will be used when we implement individual bar animation
                return super.getItemPaint(row, column);
            }
        };
        
        chart.getCategoryPlot().setRenderer(renderer);

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
                exportButton.setEnabled(true);
            }
        }
    }

    private class ProcessDataAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (selectedFile != null) {
                // Process the CSV file to get year count data
                processCSVFile();
                // Start animation
                animateBarChart();
            } else {
                JOptionPane.showMessageDialog(GameReleaseDataAnalysis.this, "Please upload a CSV file first.");
            }
        }
    }

    private void processCSVFile() {
        yearCountMap = new TreeMap<>(); // Use TreeMap to sort years

        try (BufferedReader br = new BufferedReader(new FileReader(selectedFile))) {
            String line;
            br.readLine(); // Skip header
            while ((line = br.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 8) {
                    String releaseYear = columns[8].trim();
                    if (!releaseYear.isEmpty()) {
                        try {
                            int year = Integer.parseInt(releaseYear);
                            if (year >= 1993 && year <= 2020) {
                                yearCountMap.put(releaseYear, yearCountMap.getOrDefault(releaseYear, 0) + 1);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid year format: " + releaseYear);
                        }
                    }
                }
            }
        } catch (IOException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error reading file: " + ex.getMessage());
        }
    }

    private void animateBarChart() {
        // Clear any existing data
        dataset.clear();
        
        // Initialize the dataset with zero values
        for (String year : yearCountMap.keySet()) {
            dataset.addValue(0, "Games", year);
        }
        
        // Reset animation step
        animationStep = 0;
        
        // Disable the process button during animation
        processButton.setEnabled(false);
        
        // Use custom renderer for the animation
        final BarRenderer renderer = (BarRenderer) chart.getCategoryPlot().getRenderer();
        
        // Create and start animation timer
        animationTimer = new Timer(TIMER_DELAY_MS, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                animationStep++;
                
                // Update each bar with its current height based on animation progress
                for (Map.Entry<String, Integer> entry : yearCountMap.entrySet()) {
                    String year = entry.getKey();
                    int finalValue = entry.getValue();
                    
                    // Calculate current height as a percentage of final height
                    double currentHeight = finalValue * ((double) animationStep / TOTAL_ANIMATION_STEPS);
                    
                    // Update the bar in the dataset
                    dataset.setValue(currentHeight, "Games", year);
                }
                
                // Refresh the chart
                chart.fireChartChanged();
                
                // Check if animation is complete
                if (animationStep >= TOTAL_ANIMATION_STEPS) {
                    animationTimer.stop();
                    
                    // Set the final exact values
                    for (Map.Entry<String, Integer> entry : yearCountMap.entrySet()) {
                        dataset.setValue(entry.getValue(), "Games", entry.getKey());
                    }
                    
                    // Refresh chart with final values
                    chart.fireChartChanged();
                    
                    // Re-enable the process button
                    processButton.setEnabled(true);
                }
            }
        });
        
        // Start the animation
        animationTimer.start();
    }

    private class ExportDataAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            if (dataset.getRowCount() == 0) {
                JOptionPane.showMessageDialog(GameReleaseDataAnalysis.this, "No data to export.");
                return;
            }

            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setDialogTitle("Specify a file to save");
            fileChooser.setSelectedFile(new File("release_year_data.csv"));

            int userSelection = fileChooser.showSaveDialog(GameReleaseDataAnalysis.this);
            if (userSelection == JFileChooser.APPROVE_OPTION) {
                File fileToSave = fileChooser.getSelectedFile();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileToSave))) {
                    writer.write("Release Year,Number of Games");
                    writer.newLine();

                    for (int i = 0; i < dataset.getColumnCount(); i++) {
                        String year = dataset.getColumnKey(i).toString();
                        Number count = dataset.getValue(0, i);
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
            chart.getCategoryPlot().getRenderer().setSeriesPaint(0, Color.CYAN);
            
            chart.getCategoryPlot().getDomainAxis().setLabelPaint(Color.WHITE);
            chart.getCategoryPlot().getRangeAxis().setLabelPaint(Color.WHITE);
            chart.getCategoryPlot().getDomainAxis().setTickLabelPaint(Color.LIGHT_GRAY);
            chart.getCategoryPlot().getRangeAxis().setTickLabelPaint(Color.LIGHT_GRAY);
            chart.getLegend().setBackgroundPaint(Color.DARK_GRAY);
            chart.getLegend().setItemPaint(Color.WHITE);
            
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
            chart.getCategoryPlot().getRenderer().setSeriesPaint(0, Color.BLUE);
            
            chart.getCategoryPlot().getDomainAxis().setLabelPaint(Color.BLACK);
            chart.getCategoryPlot().getRangeAxis().setLabelPaint(Color.BLACK);
            chart.getCategoryPlot().getDomainAxis().setTickLabelPaint(Color.BLACK);
            chart.getCategoryPlot().getRangeAxis().setTickLabelPaint(Color.BLACK);
            chart.getLegend().setBackgroundPaint(Color.WHITE);
            chart.getLegend().setItemPaint(Color.BLACK);
            
            JPanel buttonPanel = (JPanel) getContentPane().getComponent(1);
            buttonPanel.setBackground(Color.LIGHT_GRAY);
        }
        chartPanel.repaint();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(GameReleaseDataAnalysis::new);
    }
}
