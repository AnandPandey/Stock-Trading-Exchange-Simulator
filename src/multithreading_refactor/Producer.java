package multithreading_refactor;

public class Producer implements Runnable {
    private SharedBuffer buffer;
    public Producer(SharedBuffer buffer) {
        this.buffer=buffer;
    }
    public void run() {
        for (int i=1; i<=10;i++) {
            buffer.produce(i);
        }
    }
}
