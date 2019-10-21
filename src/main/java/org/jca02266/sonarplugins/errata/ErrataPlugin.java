package org.jca02266.sonarplugins.errata;

import org.jca02266.sonarplugins.errata.rules.CreateIssuesOnTextFilesSensor;
import org.jca02266.sonarplugins.errata.rules.TextRulesDefinition;
import org.sonar.api.Plugin;

/**
 * This class is the entry point for all extensions. It is referenced in pom.xml.
 */
public class ErrataPlugin implements Plugin {

  @Override
  public void define(Context context) {
    context.addExtensions(TextRulesDefinition.class, CreateIssuesOnTextFilesSensor.class);
  }
}
