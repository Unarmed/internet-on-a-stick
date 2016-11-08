package se.carlengstrom.internetonastick.model.properties;

import se.carlengstrom.internetonastick.job.Job;
import se.carlengstrom.internetonastick.model.Markov;
import se.carlengstrom.internetonastick.model.properties.feeder.SentenceFeeder;

/**
 * Created by eng on 2016-11-02.
 */
public class BuilderParameters {

  final Markov markov;

  final SentenceFeeder feeder;

  final Job job;

  public BuilderParameters(
      final Markov markov,
      final SentenceFeeder feeder,
      final Job job) {
    this.markov = markov;
    this.feeder = feeder;
    this.job = job;
  }

  public Markov getMarkov() {
    return markov;
  }

  public SentenceFeeder getFeeder() {
    return feeder;
  }

  public Job getJob() {
    return job;
  }
}
