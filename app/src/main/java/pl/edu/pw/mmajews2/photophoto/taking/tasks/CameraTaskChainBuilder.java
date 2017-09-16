package pl.edu.pw.mmajews2.photophoto.taking.tasks;

import java.util.Deque;
import java.util.LinkedList;

/**
 * Created by Maciej Majewski on 2016-05-02.
 */
public class CameraTaskChainBuilder {
    private Deque<CameraTask> tasks = new LinkedList<>();

    public CameraTaskChainBuilder chainTask(CameraTask task){
        tasks.addFirst(task);
        return this;
    }

    public CameraTask buildChain(){
        CameraTask task = tasks.pollFirst();
        do{
            tasks.peekFirst().setNextTask(task);
            task = tasks.pollFirst();
        }while(!tasks.isEmpty());
        return task;
    }
}
