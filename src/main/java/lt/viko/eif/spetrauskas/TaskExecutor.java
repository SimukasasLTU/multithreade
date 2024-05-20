package lt.viko.eif.spetrauskas;

public class TaskExecutor extends Thread {
    private final Task task;

    public TaskExecutor(Task task) {
        this.task = task;
    }

    @Override
    public void run() {
        task.execute();
    }
}
