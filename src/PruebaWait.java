public class PruebaWait implements Runnable{
    private volatile boolean ejecuta1 = false;
    public synchronized void m1() {
        for (int i = 0; i < 11; i++) {
            System.out.println(i);
            if (i == 5) {
                try {
                    this.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }
    public synchronized void m2() {
        for (int i = 10; i < 21; i++) {
            System.out.println(i);
        }
        this.notifyAll();
    }
    @Override
    public void run() {
        if (!ejecuta1) {
            ejecuta1 = true;
            m1();
        } else {
            m2();
        }
    }

    static void main() {
        PruebaWait wns = new PruebaWait();
        new Thread(wns).start();
        new Thread(wns).start();
    }
}
