import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * TRABALHO PRÁTICO 01 – ALGORITMOS DE EXCLUSÃO MÚTUA
 *
 * Hélio Potelicki, Luis Felipe Zaguini e Pedro Henrique Roweder
 */
public class Process {

    private static Integer MINIMUM_SECONDS_CONSUME = 5;
    private static Integer MAXIMUM_SECONDS_CONSUME = 15;
    private static Integer MINIMUM_SECONDS_INTERVAL = 10;
    private static Integer MAXIMUM_SECONDS_INTERVAL = 25;
    private Thread askForConsumeResource;
    private Thread consumeResource;
    private int pid;

    public Process(int pid) {
        System.out.printf("%s - Novo processo criado: %s\n", getTimeNow(), pid);
        setPid(pid);
        startAskForConsumeResource();
    }

    private void startAskForConsumeResource() {
        final Process process = this;

        askForConsumeResource = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {

                    try {
                        int randomUsageTime;
                        randomUsageTime = getRandomAskInterval();
                        System.out.printf("%s - Processo " + process.getPid() + " vai aguardar " + (randomUsageTime / 1000) + "s antes de tentar consumir o recurso\n", getTimeNow());
                        Thread.sleep(randomUsageTime);

                    } catch (InterruptedException e) {
                    }

                    try {
                        if (Cluster.getInstance().getCoordinator().getProcess().getPid() != process.getPid()) {
                            if (Cluster.getInstance().getCoordinator() != null
                                    && !Cluster.getInstance().getCoordinator().isProcessInQueue(process)) {
                                System.out.printf("%s - Processo %s solicita consumir recurso\n", getTimeNow(),
                                        process.getPid());
                                Cluster.getInstance().getCoordinator().requestConsume(process);
                            }
                        }
                    } catch (NullPointerException e) {
                    }
                }
            }
        });

        askForConsumeResource.start();

    }

    public Process election() {
        List<Process> processes = Cluster.getInstance().getProcesses();
        Process newCoordinator = this;

        for (Process p : processes) {
            if (p.getPid() > newCoordinator.getPid()) {
                newCoordinator = p;
            }

        }
        System.out.printf("%s - Coordenador eleito: %s\n", getTimeNow(), newCoordinator.getPid());
        return newCoordinator;
    }

    public void ConsumeResource() {
        final Process process = this;

        consumeResource = new Thread(new Runnable() {
            int randomConsumeTime;

            @Override
            public void run() {
                randomConsumeTime = getRandomConsumeTime();

                try {
                    Thread.sleep(randomConsumeTime);
                } catch (InterruptedException e) {
                }

                Cluster.getInstance().getCoordinator().notifyStopConsume();
                System.out.printf("%s - Processo %s terminou de consumir o recurso. Consumiu por %ss\n\n",
                        getTimeNow(), process.getPid(), (randomConsumeTime / 1000));

            }
        });
        consumeResource.start();
    }

    private int getRandomAskInterval() {
        return ((MINIMUM_SECONDS_INTERVAL + (new Random().nextInt(MAXIMUM_SECONDS_INTERVAL - MINIMUM_SECONDS_INTERVAL)))
                * 1000);
    }

    private int getRandomConsumeTime() {
        return ((MINIMUM_SECONDS_CONSUME + (new Random().nextInt(MAXIMUM_SECONDS_CONSUME - MINIMUM_SECONDS_CONSUME)))
                * 1000);
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    public int getPid() {
        return this.pid;
    }

    public String getTimeNow() {
        Calendar rightNow = Calendar.getInstance();
        int hours = rightNow.get(Calendar.HOUR);
        int minutes = rightNow.get(Calendar.MINUTE);
        int seconds = rightNow.get(Calendar.SECOND);
        String time = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        return time;
    }

}