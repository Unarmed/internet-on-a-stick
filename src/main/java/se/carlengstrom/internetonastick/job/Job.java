/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package se.carlengstrom.internetonastick.job;

/**
 *
 * @author Eng
 */
public abstract class Job implements Runnable {
    public String statusString;
    public boolean isComplete;
}