package org.jca02266.sonarplugins.errata.rules;

import org.sonar.api.batch.fs.FileSystem;
import org.sonar.api.batch.fs.InputFile;
import org.sonar.api.batch.sensor.Sensor;
import org.sonar.api.batch.sensor.SensorContext;
import org.sonar.api.batch.sensor.SensorDescriptor;
import org.sonar.api.batch.sensor.issue.NewIssue;
import org.sonar.api.batch.sensor.issue.NewIssueLocation;

public class CreateIssuesOnTextFilesSensor implements Sensor {

  private static final double ARBITRARY_GAP = 1.0;

  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("Check errata for text files");

    descriptor.onlyOnLanguage("java");
    descriptor.createIssuesForRuleRepositories(TextRulesDefinition.REPOSITORY);
  }

  @Override
  public void execute(SensorContext context) {
    FileSystem fs = context.fileSystem();
    Iterable<InputFile> textFiles = fs.inputFiles(fs.predicates().all());
    for (InputFile textFile : textFiles) {
      NewIssue newIssue = context.newIssue()
        .forRule(TextRulesDefinition.RULE)
        .gap(ARBITRARY_GAP);

      NewIssueLocation primaryLocation = newIssue.newLocation()
        .on(textFile)
        .at(textFile.selectLine(1))
        // .at(textFile.newRange(textFile.newPointer(1, 10), textFile.newPointer(2,5)))
        .message("Fix typo");
      newIssue.at(primaryLocation);

      newIssue.save();
    }
  }
}
