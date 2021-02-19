package sample.Record;

import sample.Threads.RecordClass;
import sample.Threads.ViewImage;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class StartRecord {
    private ExecutorService execute=null;
    private ViewImage viewImage;
    private RecordClass recordClass;
    public StartRecord(ViewImage viewImage,RecordClass recordClass){
       this.recordClass=recordClass;
       this.viewImage=viewImage;



    }
    public void newSchedule() {
        this.execute=  Executors.newFixedThreadPool(2);

        execute.execute(recordClass);
        execute.execute(viewImage);

    }
    public boolean shutDown(){
        if(execute!=null&&!execute.isShutdown())
          execute.shutdown();
        else
            return false;
        return true;
    }
}
