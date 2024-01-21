package com.paypal.jsse.tester;


import io.netty.util.concurrent.DefaultThreadFactory;
import io.netty.util.concurrent.ThreadPerTaskExecutor;
import io.netty.util.internal.DefaultPriorityQueue;
import io.netty.util.internal.PriorityQueueNode;
import io.netty.util.internal.StringUtil;

import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;


public class QueueTest {

    public static void main(String[] args) throws Exception{
        //testQueue();
        testQueue2();
    }

    private static void testQueue2(){
        //Node.Null Found command: removeStart
        //Node.Null Found nodeStr: deadlineNanos : 152184647175 , id : 4201 , index : 0
        //Node.Null Found queueStr: [{"id" : 4201,"deadlineNanos" : 152184647175,"index" : 0,},{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },]
        //Node.Null Found queueAtStart: [{"id" : 4201,"deadlineNanos" : 152184647175,"index" : 0,},{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },{"data" : null },]


    }

    private static final Comparator<PriorityQueueNodeT> SCHEDULED_FUTURE_TASK_COMPARATOR =
            new Comparator<PriorityQueueNodeT>() {
                @Override
                public int compare(PriorityQueueNodeT o1, PriorityQueueNodeT o2) {
                    return o1.compareTo(o2);
                }
            };

    private static DefaultPriorityQueue scheduledTaskQueue;
    public static void testQueue() throws Exception{
        scheduledTaskQueue = new DefaultPriorityQueue<PriorityQueueNodeT>(
                SCHEDULED_FUTURE_TASK_COMPARATOR,
                // Use same initial capacity as java.util.PriorityQueue
                11);

        for(String order : Files.readAllLines(new File("/Users/radhakrishnan/Desktop/order.txt").toPath())){
            String command = order.split("#")[0];

            if(StringUtil.isNullOrEmpty(order)){
                continue;
            }

            System.out.println(order);

            if(command.equals("odder")){
                scheduledTaskQueue.offer(new PriorityQueueNodeT(Integer.parseInt(order.split("#")[1])));
            }else if(command.equals("removeTyped")){
                scheduledTaskQueue.removeTyped(new PriorityQueueNodeT(Integer.parseInt(order.split("#")[1])));
            }else {
                throw new Exception("error to parse command " + order);
            }
        }

       /* CompletableFuture.runAsync(() -> addTask());

        CompletableFuture.runAsync(() -> cancelTasks());

        CompletableFuture.runAsync(() -> monitor());

        try {
            Thread.sleep(60000l);
        }catch (Exception e){}*/
    }


    private static void monitor(){
        while (true) {
            try {
                PriorityQueueNode nodeT = scheduledTaskQueue.poll();
                System.out.println("Polling node : " + nodeT);
                System.out.println("Queue size : " + scheduledTaskQueue.size());
                Thread.sleep(10);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static void cancelTasks(){
        Executor executor = Executors.newFixedThreadPool(10);
        while (true){
           try {
               for(PriorityQueueNodeT key : priorityQueueNodeList.keySet()){
                   CompletableFuture.runAsync(() -> cancelTask(key), executor);
                   CompletableFuture.runAsync(() -> cancelTask(key), executor);
                   priorityQueueNodeList.remove(key);
               }
               Thread.sleep(100);
           }catch (Exception e)
           {
               e.printStackTrace();
           }

        }
    }

    /*private static void cancelTasks(){
        int taskId = 0;
        while (true){
            try {
                Thread.sleep(150l);
                if(priorityQueueNodeT != null) {
                    scheduledTaskQueue.removeTyped(priorityQueueNodeT);
                    System.out.println(priorityQueueNodeT.id + " task removed");
                }
            }catch (Exception e){e.printStackTrace();}
        }
    }*/

    private static void cancelTask(PriorityQueueNodeT task){
        try {
            scheduledTaskQueue.removeTyped(task);
            System.out.println("Cancelled Task : " + task.id);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private static Set<String> exceptions = new HashSet<>();

    private static Map<PriorityQueueNodeT, PriorityQueueNodeT> priorityQueueNodeList = new ConcurrentHashMap<>();
   private static Executor executor = new ThreadPerTaskExecutor(new DefaultThreadFactory(QueueTest.class.getClass()));


    private static void addTask() {
        int taskId = 0;
        while (true){
            //scheduledFutureTask = new ScheduledFutureTask<>(null, () -> System.out.println("task executed"),10000000000l);
            PriorityQueueNodeT priorityQueueNodeT = new PriorityQueueNodeT(taskId++);
            scheduledTaskQueue.add(priorityQueueNodeT);
            priorityQueueNodeList.put(priorityQueueNodeT, priorityQueueNodeT);
            System.out.println(taskId + " task added");
            try {
                Thread.sleep(10l);
            }catch (Exception e){e.printStackTrace();

                break;
            }
        }
    }

    private static class PriorityQueueNodeT implements PriorityQueueNode, Comparable {
        int id;

        int index = INDEX_NOT_IN_QUEUE;
        public PriorityQueueNodeT(int id){
           this.id = id;
        }

        @Override
        public int priorityQueueIndex(DefaultPriorityQueue<?> queue) {
            return index;
        }

        @Override
        public void priorityQueueIndex(DefaultPriorityQueue<?> queue, int i) {
            index = i;
        }

        /*public int compareTo(Delayed o) {

        }*/

        @Override
        public int compareTo(Object o) {
            if(o == null){
                System.out.println("Issue found !!!!!!!!!!!!!!!!!!!!!");
            }
            return id - ((PriorityQueueNodeT)o).id;
        }
    }

}
