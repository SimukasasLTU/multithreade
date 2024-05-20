package lt.viko.eif.spetrauskas;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class FileProcessingUI extends JFrame {
    public static final String SENTINEL = new String("END_OF_FILE");

    private JTextArea textArea;
    private final BlockingQueue<String> queue;
    private JProgressBar progressBar;
    private JLabel statusLabel;
    private JLabel fileLabel1;
    private JLabel fileLabel2;
    private String filePath1;
    private String filePath2;
    private int totalLines = 0;
    private JButton startButton;
    private JButton cancelButton;

    public FileProcessingUI() {
        setTitle("File Processing");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize queue
        queue = new LinkedBlockingQueue<>();
        System.out.println("Queue initialized");

        // Text area for displaying file content
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        // Progress bar
        progressBar = new JProgressBar(0, 100);
        progressBar.setStringPainted(true);
        add(progressBar, BorderLayout.SOUTH);

        // Status label
        statusLabel = new JLabel("Select files to start processing.");
        add(statusLabel, BorderLayout.NORTH);

        // File selection panel
        JPanel filePanel = new JPanel();
        filePanel.setLayout(new GridLayout(4, 2, 10, 10));

        fileLabel1 = new JLabel("File 1: Not Selected");
        fileLabel2 = new JLabel("File 2: Not Selected");
        JButton selectFile1Button = new JButton("Select File 1");
        JButton selectFile2Button = new JButton("Select File 2");
        startButton = new JButton("Start Processing");
        startButton.setEnabled(false);
        cancelButton = new JButton("Cancel");
        cancelButton.setEnabled(false);
        JButton clearButton = new JButton("Clear");

        filePanel.add(fileLabel1);
        filePanel.add(selectFile1Button);
        filePanel.add(fileLabel2);
        filePanel.add(selectFile2Button);
        filePanel.add(clearButton);
        filePanel.add(startButton);
        filePanel.add(new JLabel()); // empty space
        filePanel.add(cancelButton);

        add(filePanel, BorderLayout.WEST);

        selectFile1Button.addActionListener(e -> selectFile(1, startButton));
        selectFile2Button.addActionListener(e -> selectFile(2, startButton));
        startButton.addActionListener(e -> startProcessing());
        cancelButton.addActionListener(e -> cancelProcessing());
        clearButton.addActionListener(e -> clearUI());
    }

    private void selectFile(int fileNumber, JButton startButton) {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            String filePath = selectedFile.getAbsolutePath();
            System.out.println("Selected file " + fileNumber + ": " + filePath);
            if (fileNumber == 1) {
                filePath1 = filePath;
                fileLabel1.setText("File 1: " + selectedFile.getName());
            } else if (fileNumber == 2) {
                filePath2 = filePath;
                fileLabel2.setText("File 2: " + selectedFile.getName());
            }
            if (filePath1 != null && filePath2 != null) {
                startButton.setEnabled(true);
                statusLabel.setText("Files selected. Ready to start processing.");
            }
        }
    }

    private void startProcessing() {
        System.out.println("Queue retrieved: " + (queue != null));

        Task fileReadingTask1 = new FileReadingTask(filePath1, queue, this);
        Task fileReadingTask2 = new FileReadingTask(filePath2, queue, this);
        Task fileWritingTask = new FileWritingTask("output.txt", queue, this);

        TaskExecutor reader1 = new TaskExecutor(fileReadingTask1);
        TaskExecutor reader2 = new TaskExecutor(fileReadingTask2);
        TaskExecutor writer = new TaskExecutor(fileWritingTask);

        reader1.start();
        reader2.start();
        writer.start();

        statusLabel.setText("Processing started...");
        startButton.setEnabled(false);
        cancelButton.setEnabled(true);
    }

    private void cancelProcessing() {
        // Implement logic to stop the threads gracefully
        System.out.println("Processing cancelled.");
        statusLabel.setText("Processing cancelled.");
        startButton.setEnabled(true);
        cancelButton.setEnabled(false);
    }

    public synchronized void appendText(String text) {
        SwingUtilities.invokeLater(() -> textArea.append(text + "\n"));
    }

    public void updateProgressBar(int processedLines) {
        SwingUtilities.invokeLater(() -> progressBar.setValue((int) ((processedLines / (double) totalLines) * 100)));
    }

    public synchronized void incrementTotalLines(int count) {
        totalLines += count;
        SwingUtilities.invokeLater(() -> progressBar.setMaximum(totalLines));
    }

    private void clearUI() {
        textArea.setText("");
        progressBar.setValue(0);
        statusLabel.setText("Select files to start processing.");
        fileLabel1.setText("File 1: Not Selected");
        fileLabel2.setText("File 2: Not Selected");
        filePath1 = null;
        filePath2 = null;
        totalLines = 0;
        startButton.setEnabled(false);
        cancelButton.setEnabled(false);
    }

    public BlockingQueue<String> getQueue() {
        return queue;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            FileProcessingUI ui = new FileProcessingUI();
            ui.setVisible(true);
        });
    }
}
