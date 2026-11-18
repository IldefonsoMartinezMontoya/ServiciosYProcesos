public class Prueba2 extends Thread{
    private int id;
    private boolean suspender = false;
    private  Thread hiloReferencia;
    public Prueba2(int id) {
        this.id = id;
    }
    public void suspenderHilo(Thread hiloReferencia) {
        this.suspender = true;
        this.hiloReferencia = hiloReferencia;
    }
    @Override
    public void run() {
        try {
            for (int i = 0; i < 3; i++) {
                if (suspender) {
                    hiloReferencia.join();
                }
                System.out.printf("Hilo %d. Iteración %d \n", id, i);
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    static void main() {
        Prueba2 hilo1 = new Prueba2(1);
        Prueba2 hilo2 = new Prueba2(2);
        hilo1.start();
        hilo2.start();
        hilo2.suspenderHilo(hilo1);
    }
}
