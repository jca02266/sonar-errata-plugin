package org.jca02266.sonarplugins.errata.settings;

import static java.util.Arrays.asList;

import java.util.List;

import org.sonar.api.config.PropertyDefinition;

public class ErrataProperties {

  public static final String CATEGORY = "Errata";

  private ErrataProperties() {
  }

  public static List<PropertyDefinition> getProperties() {
    return asList(
      PropertyDefinition.builder("errata.file.suffixes")
        .name("File suffixes")
        .description("Comma-separated list of suffixes for files to analyze")
        .multiValues(true)
        .defaultValue(".java")
        .category(CATEGORY)
        .build(),
      PropertyDefinition.builder("errata.table")
        .name("Errata list")
        .description("Errata which format is \"wrong word regexp -> right word\"")
        .multiValues(true)
        .category(CATEGORY)
        .build());
  }
}