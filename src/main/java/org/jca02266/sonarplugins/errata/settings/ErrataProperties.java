package org.jca02266.sonarplugins.errata.settings;

import static java.util.Arrays.asList;

import java.util.List;

import org.sonar.api.config.PropertyDefinition;

public class ErrataProperties {

  public static final String CATEGORY = "Errata Properties Example";

  private ErrataProperties() {
  }

  public static List<PropertyDefinition> getProperties() {
    return asList(
      PropertyDefinition.builder("errata.text")
        .name("Errata１")
        .description("Errata properties 1")
        .defaultValue("タイポ")
        .category(CATEGORY)
        .build(),
      PropertyDefinition.builder("errata.text2")
        .name("Errata２")
        .description("Errata properties 2")
        .multiValues(true)
        .category(CATEGORY)
        .build());
  }
}