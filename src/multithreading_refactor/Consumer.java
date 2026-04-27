package multithreading_refactor;

public class Consumer implements Runnable {
    private SharedBuffer buffer;
    public Consumer(SharedBuffer buffer) {
        this.buffer=buffer;
    }
    public void run() {
        for (int i=1; i<=10;i++) {
            buffer.consume();
        }
    }
}
