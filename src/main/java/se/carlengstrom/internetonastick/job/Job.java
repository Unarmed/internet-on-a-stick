/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.carlengstrom.internetonastick.job;

import se.carlengstrom.internetonastick.model.Markov;

/**
 *
 * @author Eng
 */
public abstract class Job implements Runnable {
    
    private Markov markov;
    private String statusString;
    private String sample;
    private JobState status;
    private long startTime;
    private long endTime;
    
    protected Job(Markov m) {
        this.markov = m;
        this.statusString = "Not Started";
        this.sample = "Samples not available until job is running";
        this.status = JobState.QUEUED;
    }
    
    public Markov getMarkov() {
        return markov;
    }
    
    public void setStatus(String status) {
        this.statusString = status;
    }
    
    public String getStatusString() {
        return statusString;
    }
    
    public void setSample(String sample) {
        this.sample = sample;
    }
    
    public String getSample() {
        return sample;
    }
    
    public JobState getStaus() {
        return status;
    }
    
    public long getDuration() {
        switch(status) {
            case QUEUED:
                return 0;
            case IN_PROGRESS:
                return System.currentTimeMillis() - startTime;
            case DONE:
                return endTime - startTime;
            default: //Why java, why!?
                return 0;
        }
    }
    
    @Override
    public void run() {
        startTime = System.currentTimeMillis();
        status = JobState.IN_PROGRESS;
        try {
            jobRun();
            status = JobState.DONE;
        } catch (Exception e) {
            status = JobState.FAILED;
            statusString = "Job failed due to " + e.getClass().getName() + ": " + e.getMessage();
            e.printStackTrace();
        } finally {
            endTime = System.currentTimeMillis();
        }
    }
    
    protected abstract void jobRun() throws Exception;
}
