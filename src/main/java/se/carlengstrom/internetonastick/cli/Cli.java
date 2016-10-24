package se.carlengstrom.internetonastick.cli;

import se.carlengstrom.internetonastick.job.AppendLineFileToMarkovJob;
import se.carlengstrom.internetonastick.job.AppendLineFileToMarkovJob.Delimiter;
import se.carlengstrom.internetonastick.job.JobRunner;
import se.carlengstrom.internetonastick.job.JobState;
import se.carlengstrom.internetonastick.model.Markov;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class Cli {

  private final JobRunner runner;
  private final Map<String, Markov> inMemoryMarkovs;

  public Cli(final JobRunner runner) {
    this.runner = runner;
    this.inMemoryMarkovs = new HashMap<>();
  }

  private void runForever() {
    runner.startScheduler();

    try (final BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in))) {
      String read = null;
      System.out.println("Welcome to Internet On A Stick CLI!");
      while((read = inReader.readLine()) != null) {
        if(read.equals("quit")) {
          runner.stopScheduler();
          System.exit(0);
        } else if(read.startsWith("load")) {
          final String markovName = read.split(" ")[1];
          if(!inMemoryMarkovs.containsKey(markovName)) {
            inMemoryMarkovs.put(markovName, new Markov());
          }

          final Markov markovToAddTo = inMemoryMarkovs.get(markovName);
          final AppendLineFileToMarkovJob job =
              new AppendLineFileToMarkovJob(markovToAddTo, markovName, Delimiter.PERIOD);

          runner.scheduleJob(job);

          while(job.getStaus() != JobState.DONE && job.getStaus() != JobState.FAILED) {
            System.out.println("Job " + markovName + " has status: " + job.getStaus());
            Thread.sleep(1000);
          }

          System.out.println("Job " + markovName + " terminated with status: " + job.getStaus());
        } else {
          final Markov toUse = inMemoryMarkovs.get(read);
          if(toUse != null) {
            System.out.println(inMemoryMarkovs.get(read).generateSentence());
          } else {
            System.out.println("No such markov: " + read);
          }
        }
      }
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
    }
  }

  public static void main(String[] args) {
    JobRunner runner = new JobRunner();
    runner.startScheduler();
    final Cli cli = new Cli(runner);
    cli.runForever();
  }
}
