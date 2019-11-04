package org.jca02266.sonarplugins.errata.rules;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.regex.Pattern;

import org.junit.Test;

public class PatternSearcherTest {
  private static class Counter {
    private int counter = 0;

    public void increment() {
      counter++;
    }

    public int get() {
      return counter;
    }
  }

  @Test
  public void test_simple() throws Exception {
    String buffer = "aaaタイポbbb";

    Charset cs = Charset.forName("utf-8");
    InputStream is = new ByteArrayInputStream(buffer.getBytes(cs));

    Counter count = new Counter();

    new PatternSearcher(Pattern.compile("タイポ")).search(is, cs, linenum->start->end-> {
      assertThat(linenum, is(1));
      assertThat(start, is(3));
      assertThat(end, is(6));

      count.increment();
    });

    assertThat(count.get(), is(1));
  }

  @Test
  public void test_multiline() throws Exception {
    String buffer = "aaaタイポbbb\n"
      + "aaaタイポbbbタイポタイポccc";

    Charset cs = Charset.forName("utf-8");
    InputStream is = new ByteArrayInputStream(buffer.getBytes(cs));

    Counter count = new Counter();

    new PatternSearcher(Pattern.compile("タイポ")).search(is, cs, linenum->start->end-> {
      switch (count.get()) {
        case 0:
          assertThat(linenum, is(1));
          assertThat(start, is(3));
          assertThat(end, is(6));
          break;
        case 1:
          assertThat(linenum, is(2));
          assertThat(start, is(3));
          assertThat(end, is(6));
          break;
        case 2:
          assertThat(linenum, is(2));
          assertThat(start, is(9));
          assertThat(end, is(12));
          break;
        case 3:
          assertThat(linenum, is(2));
          assertThat(start, is(12));
          assertThat(end, is(15));
          break;
        default:
          throw new RuntimeException("default: " + count.get());
      }
      count.increment();
    });
    assertThat(count.get(), is(4));
  }
}