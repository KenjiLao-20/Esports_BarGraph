import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.*;
import java.util.List;
import javax.swing.Timer;

public class GameReleaseDataAnalysis extends JFrame {
    
    // UI Components
    private JPanel mainPanel;
    private JPanel buttonPanel;
    private JPanel chartPanel;
    private JButton processButton;
    private JButton exportButton;
    private JButton browseButton;
    private JTextField filePathField;
    private JToggleButton themeToggle;
    private CustomBarChart barChart;
    
    // Data storage
    private HashMap<Integer, Integer> releaseYearData;
    private File selectedFile;
    
    // Theme colors
    private Color lightBackground = Color.WHITE;
    private Color darkBackground = new Color(18, 18, 18);
    private Color lightText = Color.BLACK;
    private Color darkText = Color.WHITE;
    private Color lightPanelBg = new Color(248, 249, 250);
    private Color darkPanelBg = new Color(52, 58, 64);
    private Color buttonBg = new Color(0, 123, 255);
    private Color darkButtonBg = new Color(30, 136, 229);
    private boolean isDarkMode = false;
    
    public GameReleaseDataAnalysis() {
        // Set up the main frame
        super("Game Release Year Analysis");
        setSize(900, 700);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize components
        initComponents();
        
        // Layout the components
        layoutComponents();
        
        // Set the default theme
        applyTheme();
        
        setVisible(true);
    }
    
    private void initComponents() {
        // Initialize panels
        mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(new EmptyBorder(20, 20, 20, 20));
        
        buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
        buttonPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        
        chartPanel = new JPanel(new BorderLayout());
        chartPanel.setBorder(new LineBorder(Color.BLACK, 2, true));
        chartPanel.setPreferredSize(new Dimension(800, 500));
        
        // File selection components
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filePathField = new JTextField(30);
        filePathField.setEditable(false);
        browseButton = new JButton("Browse");
        
        // Action buttons
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        processButton = new JButton("Process Data");
        exportButton = new JButton("Export Data");
        exportButton.setEnabled(false);
        
        // Theme toggle
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themeToggle = new JToggleButton("🌙 Dark Mode");
        
        // Initialize the custom bar chart
        barChart = new CustomBarChart();
        barChart.setPreferredSize(new Dimension(800, 500));
        
        // Initialize data storage
        releaseYearData = new HashMap<>();
        
        // Add action listeners
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForFile();
            }
        });
        
        processButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                processData();
            }
        });
        
        exportButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                exportData();
            }
        });
        
        themeToggle.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                isDarkMode = e.getStateChange() == ItemEvent.SELECTED;
                themeToggle.setText(isDarkMode ? "☀️ Light Mode" : "🌙 Dark Mode");
                applyTheme();
            }
        });
    }
    
    private void layoutComponents() {
        // Set up file panel
        JPanel filePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        filePanel.add(filePathField);
        filePanel.add(browseButton);
        
        // Set up action panel
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        actionPanel.add(processButton);
        actionPanel.add(exportButton);
        
        // Set up theme panel
        JPanel themePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        themePanel.add(themeToggle);
        
        // Add title and panels to button panel
        JLabel titleLabel = new JLabel("Distribution of Games by Release Year", JLabel.CENTER);
        titleLabel.setFont(new Font("Arial", Font.BOLD, 20));
        
        buttonPanel.add(titleLabel);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(filePanel);
        buttonPanel.add(Box.createVerticalStrut(10));
        buttonPanel.add(actionPanel);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(themePanel);
        
        // Add chart to chart panel
        chartPanel.add(barChart, BorderLayout.CENTER);
        
        // Add all panels to main panel
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(chartPanel, BorderLayout.CENTER);
        
        // Add main panel to frame
        setContentPane(mainPanel);
    }
    
    private void applyTheme() {
        // Apply the selected theme colors
        Color bgColor = isDarkMode ? darkBackground : lightBackground;
        Color textColor = isDarkMode ? darkText : lightText;
        Color panelBgColor = isDarkMode ? darkPanelBg : lightPanelBg;
        Color currentButtonBg = isDarkMode ? darkButtonBg : buttonBg;
        
        // Update frame and panels
        mainPanel.setBackground(bgColor);
        buttonPanel.setBackground(panelBgColor);
        chartPanel.setBackground(bgColor);
        chartPanel.setBorder(new LineBorder(textColor, 2, true));
        
        // Update components in buttonPanel
        for (Component comp : buttonPanel.getComponents()) {
            if (comp instanceof JPanel) {
                comp.setBackground(panelBgColor);
                for (Component innerComp : ((JPanel) comp).getComponents()) {
                    innerComp.setBackground(panelBgColor);
                    if (innerComp instanceof JLabel) {
                        ((JLabel) innerComp).setForeground(textColor);
                    }
                }
            } else if (comp instanceof JLabel) {
                ((JLabel) comp).setForeground(textColor);
            }
        }
        
        // Update buttons
        browseButton.setBackground(currentButtonBg);
        browseButton.setForeground(Color.WHITE);
        processButton.setBackground(currentButtonBg);
        processButton.setForeground(Color.WHITE);
        exportButton.setBackground(currentButtonBg);
        exportButton.setForeground(Color.WHITE);
        
        // Update chart with theme colors
        barChart.setThemeColors(bgColor, textColor);
        
        // Repaint all components
        repaint();
    }
    
    private void browseForFile() {
        JFileChooser fileChooser = new JFileChooser();
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            selectedFile = fileChooser.getSelectedFile();
            filePathField.setText(selectedFile.getPath());
        }
    }
    
    private void processData() {
        if (selectedFile == null) {
            JOptionPane.showMessageDialog(this, "Please select a CSV file first.", 
                    "No File Selected", JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        try {
            // Read and parse CSV file
            BufferedReader reader = new BufferedReader(new FileReader(selectedFile));
            String line;
            releaseYearData.clear();
            
            // Skip header line
            reader.readLine();
            
            while ((line = reader.readLine()) != null) {
                String[] columns = line.split(",");
                if (columns.length > 8) { // Adjust this based on your actual CSV structure
                    try {
                        int releaseYear = Integer.parseInt(columns[8].trim());
                        
                        // Filter years in the appropriate range (1993-2020)
                        if (releaseYear >= 1993 && releaseYear <= 2020) {
                            releaseYearData.put(releaseYear, releaseYearData.getOrDefault(releaseYear, 0) + 1);
                        }
                    } catch (NumberFormatException e) {
                        // Skip rows with non-numeric release years
                    }
                }
            }
            reader.close();
            
            if (releaseYearData.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                        "No valid release year data found in the CSV file.",
                        "No Data", JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            // Create and display chart
            updateChart();
            
            // Enable export button
            exportButton.setEnabled(true);
            
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, 
                    "Error reading CSV file: " + e.getMessage(),
                    "File Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void updateChart() {
        // Sort the years
        List<Integer> years = new ArrayList<>(releaseYearData.keySet());
        Collections.sort(years);
        
        // Prepare data for chart
        int maxValue = 0;
        Map<String, Integer> data = new LinkedHashMap<>();
        
        for (Integer year : years) {
            int value = releaseYearData.get(year);
            data.put(year.toString(), value);
            if (value > maxValue) {
                maxValue = value;
            }
        }
        
        // Set data and animate
        barChart.setData(data, maxValue);
        barChart.startAnimation();
    }
    
    private void exportData() {
        // Create file chooser for saving the file
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Save Data");
        fileChooser.setSelectedFile(new File("release_year_data.csv"));
        FileNameExtensionFilter filter = new FileNameExtensionFilter("CSV Files", "csv");
        fileChooser.setFileFilter(filter);
        
        int result = fileChooser.showSaveDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            
            // Ensure file has .csv extension
            if (!file.getName().toLowerCase().endsWith(".csv")) {
                file = new File(file.getAbsolutePath() + ".csv");
            }
            
            try {
                FileWriter writer = new FileWriter(file);
                writer.write("Release Year,Number of Games\n");
                
                List<Integer> years = new ArrayList<>(releaseYearData.keySet());
                Collections.sort(years);
                
                for (Integer year : years) {
                    writer.write(year + "," + releaseYearData.get(year) + "\n");
                }
                
                writer.close();
                JOptionPane.showMessageDialog(this, 
                        "Data exported successfully to " + file.getName(),
                        "Export Successful", JOptionPane.INFORMATION_MESSAGE);
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                        "Error exporting data: " + e.getMessage(),
                        "Export Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Custom Bar Chart Implementation
    private class CustomBarChart extends JPanel {
        private Map<String, Integer> data;
        private Map<String, Double> currentHeights;
        private int maxValue;
        private Color barColor = new Color(75, 192, 192);
        private Color backgroundColor = Color.WHITE;
        private Color textColor = Color.BLACK;
        private Timer animationTimer;
        private static final int ANIMATION_DURATION = 1000; // milliseconds
        private static final int ANIMATION_STEPS = 20;
        private static final int PADDING = 30;
        private static final int BOTTOM_PADDING = 60;
        private static final int LEFT_PADDING = 60;
        
        public CustomBarChart() {
            this.data = new LinkedHashMap<>();
            this.currentHeights = new LinkedHashMap<>();
            setBackground(backgroundColor);
        }
        
        public void setThemeColors(Color backgroundColor, Color textColor) {
            this.backgroundColor = backgroundColor;
            this.textColor = textColor;
            setBackground(backgroundColor);
            repaint();
        }
        
        public void setData(Map<String, Integer> data, int maxValue) {
            this.data = data;
            this.maxValue = maxValue;
            this.currentHeights = new LinkedHashMap<>();
            
            // Initialize heights to zero for animation
            for (String key : data.keySet()) {
                currentHeights.put(key, 0.0);
            }
            
            repaint();
        }
        
        public void startAnimation() {
            if (animationTimer != null && animationTimer.isRunning()) {
                animationTimer.stop();
            }
            
            final int steps = ANIMATION_STEPS;
            final int delay = ANIMATION_DURATION / steps;
            
            animationTimer = new Timer(delay, new ActionListener() {
                private int step = 0;
                
                @Override
                public void actionPerformed(ActionEvent e) {
                    if (step >= steps) {
                        // Animation complete
                        for (String key : data.keySet()) {
                            currentHeights.put(key, (double) data.get(key));
                        }
                        ((Timer) e.getSource()).stop();
                    } else {
                        // Update heights for each bar
                        for (String key : data.keySet()) {
                            double targetHeight = data.get(key);
                            double newHeight = targetHeight * ((double) step / steps);
                            currentHeights.put(key, newHeight);
                        }
                        step++;
                    }
                    repaint();
                }
            });
            
            animationTimer.start();
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            if (data.isEmpty() || maxValue == 0) {
                // Draw empty chart
                drawEmptyChart(g2d);
                return;
            }
            
            int width = getWidth() - (LEFT_PADDING + PADDING);
            int height = getHeight() - (PADDING + BOTTOM_PADDING);
            
            // Draw axes
            g2d.setColor(textColor);
            g2d.drawLine(LEFT_PADDING, PADDING, LEFT_PADDING, height + PADDING);
            g2d.drawLine(LEFT_PADDING, height + PADDING, width + LEFT_PADDING, height + PADDING);
            
            // Draw Y-axis labels
            g2d.setFont(new Font("Arial", Font.PLAIN, 12));
            int yLabelCount = 5;
            for (int i = 0; i <= yLabelCount; i++) {
                int value = maxValue * i / yLabelCount;
                int y = height + PADDING - (height * i / yLabelCount);
                g2d.drawString(String.valueOf(value), 5, y + 5);
                g2d.drawLine(LEFT_PADDING - 5, y, LEFT_PADDING, y);
            }
            
            // Draw title
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String title = "Distribution of Games by Release Year";
            FontMetrics fm = g2d.getFontMetrics();
            int titleWidth = fm.stringWidth(title);
            g2d.drawString(title, (getWidth() - titleWidth) / 2, 20);
            
            // Draw X and Y axis labels
            g2d.setFont(new Font("Arial", Font.PLAIN, 14));
            g2d.drawString("Release Year", width / 2, height + PADDING + 40);
            
            // Draw Y-axis label vertically
            g2d.rotate(-Math.PI / 2, 15, height / 2);
            g2d.drawString("Number of Games", 15, height / 2);
            g2d.rotate(Math.PI / 2, 15, height / 2);
            
            // Calculate bar width based on data size
            int dataSize = data.size();
            int barWidth = Math.max(10, (width / dataSize) - 10);
            int barSpacing = (width - (barWidth * dataSize)) / (dataSize + 1);
            
            // Draw bars
            int i = 0;
            List<String> years = new ArrayList<>(data.keySet());
            for (String key : years) {
                double currentHeight = currentHeights.get(key);
                
                int x = LEFT_PADDING + barSpacing + i * (barWidth + barSpacing);
                int barHeight = (int) (height * currentHeight / maxValue);
                int y = height + PADDING - barHeight;
                
                // Draw bar
                g2d.setColor(barColor);
                g2d.fillRect(x, y, barWidth, barHeight);
                g2d.setColor(textColor);
                g2d.drawRect(x, y, barWidth, barHeight);
                
                // Draw value above bar
                String valueLabel = String.valueOf(Math.round(currentHeight));
                int labelWidth = g2d.getFontMetrics().stringWidth(valueLabel);
                g2d.drawString(valueLabel, x + (barWidth - labelWidth) / 2, y - 5);
                
                // Draw X-axis labels (only for select years to avoid crowding)
                g2d.setFont(new Font("Arial", Font.PLAIN, 12));
                
                // Show every 3 years or every 5 years if we have more than 20 data points
                boolean showLabel = (dataSize <= 20) ? 
                                    (i % 3 == 0 || i == dataSize - 1) : 
                                    (i % 5 == 0 || i == dataSize - 1);
                
                if (showLabel) {
                    g2d.drawString(key, x + (barWidth - g2d.getFontMetrics().stringWidth(key)) / 2, 
                                height + PADDING + 20);
                }
                
                i++;
            }
        }
        
        private void drawEmptyChart(Graphics2D g2d) {
            g2d.setColor(textColor);
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            String message = "No data available";
            FontMetrics fm = g2d.getFontMetrics();
            int messageWidth = fm.stringWidth(message);
            g2d.drawString(message, (getWidth() - messageWidth) / 2, getHeight() / 2);
        }
    }
    
    public static void main(String[] args) {
        // Set look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Launch application
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new GameReleaseDataAnalysis();
            }
        });
    }
}
