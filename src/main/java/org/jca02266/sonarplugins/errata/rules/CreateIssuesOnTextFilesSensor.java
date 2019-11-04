package org.jca02266.sonarplugins.errata.rules;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.sonar.api.batch.fs.FilePredicate;
import org.sonar.api.batch.fs.FilePredicates;
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
    this.config = config;
  }
  @Override
  public void describe(SensorDescriptor descriptor) {
    descriptor.name("Check errata for text files");

    descriptor.onlyOnLanguage("java");
    descriptor.createIssuesForRuleRepositories(TextRulesDefinition.REPOSITORY);
  }

  private FilePredicate addExtensions(FilePredicates fps) {
    String[] suffixes = config.getStringArray("errata.file.suffixes");
    List<FilePredicate> suffixPredicateList = Arrays.stream(suffixes).map(s -> {
        // remove "." at first
        if (s.startsWith(".")) {
          return s.substring(1);
        } else {
          return s;
        }
      })
      .map(s -> fps.hasExtension(s))
      .collect(Collectors.toList());

    return fps.or(suffixPredicateList);
  }

  @Override
  public void execute(SensorContext context) {
    List<Pair<PatternSearcher, String>> pairs = Arrays.stream(config.getStringArray("errata.table")).map(s -> {
        String[] strs = s.split("->", 2);
        LOGGER.info("errata wrong word: {}, right word: {}", strs[0], strs[1]);
        if (strs.length != 2) {
          throw new RuntimeException(String.format("invalid format on errata.table. it must be \" key -> val , ...\": \"%s\"", s));
        }
        return new Pair<PatternSearcher, String>(new PatternSearcher(Pattern.compile(strs[0].trim())), strs[1].trim());
      })
      .collect(Collectors.toList());

    FileSystem fs = context.fileSystem();
    FilePredicate fp = addExtensions(fs.predicates());
    Iterable<InputFile> textFiles = fs.inputFiles(fp);

    for (InputFile textFile : textFiles) {
      try {
        InputStream is = textFile.inputStream();
        byte[] bytes = getBytes(is);

        Charset cs = textFile.charset();

        for (Pair<PatternSearcher, String> pair: pairs) {
          pair.getFirst().search(new ByteArrayInputStream(bytes), cs, linenum -> start -> end ->
            registerIssue(context, textFile, linenum, start, end, pair.getSecond())
          );
        }
      } catch (IOException e) {
        throw new RuntimeException("failed to inputStream()", e);
      }
    }
  }

  private void registerIssue(SensorContext context, InputFile textFile, int line, int start, int end, String rightWord) {
    NewIssue newIssue = context.newIssue()
      .forRule(TextRulesDefinition.RULE)
      .gap(ARBITRARY_GAP);

    NewIssueLocation primaryLocation = newIssue.newLocation()
      .on(textFile)
      .at(textFile.newRange(textFile.newPointer(line, start), textFile.newPointer(line, end)))
      .message("May be typo: " + rightWord);
    newIssue.at(primaryLocation);

    newIssue.save();
  }

  static class Pair<T,U> {
    T first;
    U second;
    Pair(T first, U second) {
      this.first = first;
      this.second = second;
    }
    public T getFirst() { return first; }
    public U getSecond() { return second; }
  }

  public static byte[] getBytes(InputStream is) throws IOException {
    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    byte[] bytes = new byte[512];

    for (int len = is.read(bytes); len != -1; len = is.read(bytes)) {
      baos.write(bytes, 0, len);
    }

    return baos.toByteArray();
  }
}
