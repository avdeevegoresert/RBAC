import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class MultiThreadProgress {
    
    private static final int THREAD_COUNT = 5;
    private static final int PROGRESS_LENGTH = 50;
    private static final int UPDATE_DELAY_MS = 100;
    
    public static void main(String[] args) {
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        
        System.out.println("\n Многопоточный расчёт");
        System.out.println("Потоков: " + THREAD_COUNT);
        System.out.println("Длина прогресс-бара: " + PROGRESS_LENGTH);
        
        
        for (int i = 0; i < THREAD_COUNT; i++) {
            final int threadNum = i + 1;
            
            executor.submit(() -> {
                long startTime = System.nanoTime();
                long threadId = Thread.currentThread().threadId();
                
                for (int progress = 0; progress <= PROGRESS_LENGTH; progress++) {
                    long elapsed = System.nanoTime() - startTime;
                    long elapsedMs = TimeUnit.NANOSECONDS.toMillis(elapsed);
                    
                    printProgress(threadNum, threadId, progress, PROGRESS_LENGTH, elapsedMs);
                    
                    try {
                        Thread.sleep(UPDATE_DELAY_MS);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }
                
                long totalTimeMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startTime);
                printCompletion(threadNum, threadId, totalTimeMs);
            });
        }
        
        executor.shutdown();
        try {
            executor.awaitTermination(60, TimeUnit.SECONDS);
            System.out.println("\nВсе потоки завершили работу");
        } catch (InterruptedException e) {
            System.err.println("Прерывание: " + e.getMessage());
            executor.shutdownNow();
        }
    }
    
    private static synchronized void printProgress(int threadNum, long threadId, int progress, int max, long elapsedMs) {
        System.out.print(String.format("\r[%02d] ID:%d [", threadNum, threadId));
        
        for (int i = 0; i < progress; i++) {
            System.out.print("█");
        }
        for (int i = progress; i < max; i++) {
            System.out.print("░");
        }
        
        int percent = (progress * 100) / max;
        System.out.printf("] %3d%% (%d ms)\n", percent, elapsedMs);
    }
    
    private static synchronized void printCompletion(int threadNum, long threadId, long totalTimeMs) {
        System.out.printf("[%02d] ID:%d Завершен Время: %d ms\n", threadNum, threadId, totalTimeMs);
    }
}
