package multithreading_refactor;

public class Main {
    public static void main(String[] args) {
        SharedBuffer buff=new SharedBuffer();
        Thread t1=new Thread(new Producer(buff));
        Thread t2=new Thread(new Consumer(buff));
        t1.start();
        t2.start();
    }

}


