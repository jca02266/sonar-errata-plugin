package org.jca02266.sonarplugins.errata.rules;

import org.sonar.api.rule.RuleKey;
import org.sonar.api.rule.RuleStatus;
import org.sonar.api.rule.Severity;
import org.sonar.api.rules.RuleType;
import org.sonar.api.server.rule.RulesDefinition;

public class TextRulesDefinition implements RulesDefinition {

  public static final String REPOSITORY = "text-errata";
  public static final String LANGUAGE = "java";
  public static final RuleKey RULE = RuleKey.of(REPOSITORY, "errata");

  @Override
  public void define(Context context) {
    NewRepository repository = context.createRepository(REPOSITORY, LANGUAGE).setName("Check for text files");

    NewRule rule = repository.createRule(RULE.rule())
      .setName("Errata")
      .setHtmlDescription("Check errata for all text files")
      .setTags("errata")
      .setStatus(RuleStatus.READY)
      .setType(RuleType.BUG)
      .setSeverity(Severity.MINOR);

    rule.setDebtRemediationFunction(rule.debtRemediationFunctions().linearWithOffset("0h", "1min"));

    repository.done();
  }
}
