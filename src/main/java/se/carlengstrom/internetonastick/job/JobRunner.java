/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.carlengstrom.internetonastick.job;

import se.carlengstrom.internetonastick.model.Markov;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;

/**
 *
 * @author Eng
 */
public class JobRunner {
    
    private boolean isRunning;
    private final ConcurrentMap<Markov, ConcurrentLinkedQueue<Job>> jobs;
    private final Map<Markov, Thread> threads;
    
    public JobRunner() {
        jobs = new ConcurrentHashMap<>();
        threads = new HashMap<>();
    }
    
    public void startScheduler() {
        isRunning = true;
        new Thread(() -> {
            try {
                while(isRunning) {
                    for(Markov m : jobs.keySet()) {
                        if(!threads.containsKey(m)) {
                            forkThread(m);
                        }
                    }
                    Thread.sleep(1000);
                }
                } catch (InterruptedException ex) {
                    isRunning = false;
                }
            }
        , "Job discovering thread").start();
    }
    
    private void forkThread(Markov m) {
        Thread t = new Thread(() -> {
            try {
                ConcurrentLinkedQueue<Job> myJobs = jobs.get(m);
                while (isRunning) {
                    if (!myJobs.isEmpty()) {
                        Job j = myJobs.poll();
                        if (j != null) {
                            j.run();
                        }
                    } else {
                        Thread.sleep(1000);
                    }
                }
            } catch (InterruptedException ie) {
                //Crash boom bang
            }
        });        
        t.start();
        threads.put(m, t);
    }
    
    public void scheduleJob(Job j) {
        Markov m = j.getMarkov();
        ConcurrentLinkedQueue<Job> newQueue = new ConcurrentLinkedQueue<>();
        ConcurrentLinkedQueue<Job> queue = jobs.putIfAbsent(m, newQueue);
        
        if(queue == null) {
            newQueue.add(j);
        } else {
            queue.add(j);
        }
    }


    public void stopScheduler() {
        isRunning = false;
    }
}
