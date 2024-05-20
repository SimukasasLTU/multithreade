package lt.viko.eif.spetrauskas;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class FileWritingTask implements Task {
    private final String filePath;
    private final BlockingQueue<String> queue;
    private final FileProcessingUI ui;
    private int processedLines = 0;

    public FileWritingTask(String filePath, BlockingQueue<String> queue, FileProcessingUI ui) {
        this.filePath = filePath;
        this.queue = queue;
        this.ui = ui;
    }

    @Override
    public void execute() {
        System.out.println("Starting to write to file: " + filePath);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            int sentinelCount = 0;
            while (true) {
                String line = queue.take();
                if (line == FileProcessingUI.SENTINEL) {
                    sentinelCount++;
                    if (sentinelCount == 2) { // Expecting two sentinel objects, one from each reading task
                        break;
                    }
                    continue;
                }
                System.out.println("Writing line: " + line); // Debugging statement
                writer.write(line);
                writer.newLine();
                processedLines++;
                ui.updateProgressBar(processedLines); // Update progress bar
                ui.appendText(line); // Update UI with each written line
            }
            System.out.println("Finished writing to file: " + filePath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
