package App_jar;

import java.util.Arrays;

public class ProcesosNumeros implements Runnable{
    @Override
    public void run() {
        int[] nums = new int[10];
        for (int i = 0; i < 10; i++) {
            nums[i] = i;
            try {
                Thread.sleep(400);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        System.out.println(Arrays.toString(nums));
    }
}
