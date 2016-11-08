package se.carlengstrom.internetonastick.cli;

import se.carlengstrom.internetonastick.job.AppendLineFileToMarkovJob;
import se.carlengstrom.internetonastick.job.JobRunner;
import se.carlengstrom.internetonastick.job.JobState;
import se.carlengstrom.internetonastick.model.Markov;
import se.carlengstrom.internetonastick.model.properties.feeder.EnglishSentenceFeeder;
import se.carlengstrom.internetonastick.model.properties.feeder.NewLineDelimitedFeeder;
import se.carlengstrom.internetonastick.model.properties.feeder.SentenceFeeder;
import se.carlengstrom.internetonastick.model.properties.joiner.BasicJoiners;
import se.carlengstrom.internetonastick.model.properties.splitter.BasicSplitters;

import java.io.*;
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
        } else if(read.startsWith("create")) {
          final String[] parts = read.split(" ");
          final String markovName = parts[1];
          inMemoryMarkovs.put(markovName, new Markov(
                    BasicSplitters.SPLITTERS.get(parts[2]),
                    Integer.parseInt(parts[3]),
                    BasicJoiners.JOINERS.get(parts[4])));
          System.out.println("Created new empty markov chain");
        } else if (read.startsWith("load")) {
          final String[] parts = read.split(" ");
          final String markovName = parts[1];
          final Markov markovToAddTo = inMemoryMarkovs.get(markovName);
          final AppendLineFileToMarkovJob job =
              new AppendLineFileToMarkovJob(markovToAddTo, getFeeder(parts[2], parts[3]));

          runner.scheduleJob(job);

          while(job.getStaus() != JobState.DONE && job.getStaus() != JobState.FAILED) {
            System.out.println("Job " + markovName + " has status: " + job.getStaus() + " (" + job.getStatusString() + ")");
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

  private SentenceFeeder getFeeder(final String name, final String file) throws FileNotFoundException {
    switch (name) {
      case "english":
        return new EnglishSentenceFeeder(new FileInputStream(file));
      case "newline":
        return new NewLineDelimitedFeeder(new FileInputStream(file));
      default:
        throw new RuntimeException("No such sentence feeder" + name);
    }
  }

  public static void main(final String[] args) {
    JobRunner runner = new JobRunner();
    runner.startScheduler();
    final Cli cli = new Cli(runner);
    cli.runForever();
  }
}
