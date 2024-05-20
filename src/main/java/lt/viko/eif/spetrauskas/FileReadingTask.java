package lt.viko.eif.spetrauskas;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class FileReadingTask implements Task {
    private final String filePath;
    private final BlockingQueue<String> queue;
    private final FileProcessingUI ui;

    public FileReadingTask(String filePath, BlockingQueue<String> queue, FileProcessingUI ui) {
        this.filePath = filePath;
        this.queue = queue;
        this.ui = ui;
    }

    @Override
    public void execute() {
        System.out.println("Starting to read file: " + filePath);
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            int lineCount = 0;
            while ((line = reader.readLine()) != null) {
                lineCount++;
                System.out.println("Read line: " + line);
                queue.put(line);
            }
            queue.put(FileProcessingUI.SENTINEL); // Indicate end of file
            ui.incrementTotalLines(lineCount);
            System.out.println("Finished reading file: " + filePath);
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}
