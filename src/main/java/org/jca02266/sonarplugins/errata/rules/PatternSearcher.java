package org.jca02266.sonarplugins.errata.rules;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class PatternSearcher {
  Pattern pattern;

  public PatternSearcher(Pattern pattern) {
    this.pattern = pattern;
  }

  public void search(InputStream is, Charset cs, Function<Integer, Function<Integer, Consumer<Integer>>> consumer) {
    try (BufferedReader br = new BufferedReader(new InputStreamReader(is, cs))) {
      String line;
      int linenum = 0;
      while ((line = br.readLine()) != null) {
        linenum++;
        Matcher mat = pattern.matcher(line);
        int end = 0;
        while (mat.find(end)) {
          int start = mat.start();
          end = mat.end();

          consumer.apply(linenum).apply(start).accept(end);
        }
      }
    } catch (IOException e) {
      throw new RuntimeException("error", e);
    }
  }
}