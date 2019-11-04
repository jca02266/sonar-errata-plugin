package org.jca02266.sonarplugins.errata.rules;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.regex.Pattern;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;
import org.sonar.api.config.Configuration;
import org.sonar.api.utils.log.Logger;
import org.sonar.api.utils.log.Loggers;

public class CreateIssuesOnTextFilesSensor implements Sensor {
  private static final Logger LOGGER = Loggers.get(CreateIssuesOnTextFilesSensor.class);

  private static final double ARBITRARY_GAP = 1.0;

  protected final Configuration config;

  public CreateIssuesOnTextFilesSensor(final Configuration config) {
    LOGGER.info("errata sensor constructor called");
    this.config = config;
  }
  @Override
  public void describe(SensorDescriptor descriptor) {
    LOGGER.info("errata sensor describe() called");
    descriptor.name("Check errata for text files");

    descriptor.onlyOnLanguage("java");
    descriptor.createIssuesForRuleRepositories(TextRulesDefinition.REPOSITORY);
  }

  @Override
  public void execute(SensorContext context) {
    LOGGER.info("errata sensor execute() called");

    LOGGER.info("errata text: {}", config.get("errata.text").orElse("default"));
    Arrays.stream(config.getStringArray("errata.text2")).forEach(val -> LOGGER.info("errata text2: {}", val));

    FileSystem fs = context.fileSystem();
    Iterable<InputFile> textFiles = fs.inputFiles(fs.predicates().all());
    for (InputFile textFile : textFiles) {
      InputStream is;
      try {
        is = textFile.inputStream();
      } catch (IOException e) {
        throw new RuntimeException("failed to inputStream()", e);
      }

      Charset cs = textFile.charset();
      Pattern pat = Pattern.compile("タイポ");

      new PatternSearcher(pat).search(is, cs, linenum -> start -> end ->
        registerIssue(context, textFile, linenum, start, end)
      );
    }
  }

  private void registerIssue(SensorContext context, InputFile textFile, int line, int start, int end) {
    NewIssue newIssue = context.newIssue()
      .forRule(TextRulesDefinition.RULE)
      .gap(ARBITRARY_GAP);

    NewIssueLocation primaryLocation = newIssue.newLocation()
      .on(textFile)
      .at(textFile.newRange(textFile.newPointer(line, start), textFile.newPointer(line, end)))
      .message("Fix typo");
    newIssue.at(primaryLocation);

    newIssue.save();
  }
}
