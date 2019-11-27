try:
    import ptest
except ImportError:
    raise NameError("No ptest runner found in selected interpreter.")

try:
    from tc_messages import TeamcityServiceMessages
except ImportError:
    raise NameError("No tc_messages module found in selected interpreter.")

from ptest.plistener import TestListener
from ptest.enumeration import TestCaseStatus


class TeamcityTestListener(TestListener):
    def __init__(self):
        self.messages = TeamcityServiceMessages(prepend_linebreak=True)

    def on_test_suite_start(self, test_suite):
        self.messages.testMatrixEntered()
        self.messages.testCount(len(test_suite.test_cases))

    def on_test_suite_finish(self, test_suite):
        pass

    def on_test_class_start(self, test_class):
        self.messages.testSuiteStarted(suiteName=test_class.full_name)

    def on_test_class_finish(self, test_class):
        self.messages.testSuiteFinished(suiteName=test_class.full_name)

    def on_test_group_start(self, test_group):
        if not hasattr(test_group.test_class, "is_group_feature_used") or test_group.test_class.is_group_feature_used:
            self.messages.testSuiteStarted(suiteName=test_group.name)

    def on_test_group_finish(self, test_group):
        if not hasattr(test_group.test_class, "is_group_feature_used") or test_group.test_class.is_group_feature_used:
            self.messages.testSuiteFinished(suiteName=test_group.name)

    def on_test_case_start(self, test_case):
        self.messages.testStarted(testName=test_case.name, location=test_case.location)

    def on_test_case_finish(self, test_case):
        if test_case.status == TestCaseStatus.FAILED:
            self.messages.testFailed(testName=test_case.name)
        elif test_case.status == TestCaseStatus.SKIPPED:
            self.messages.testIgnored(testName=test_case.name)
        self.messages.testFinished(testName=test_case.name, duration=int(test_case.elapsed_time * 1000.0))


def _main():
    from ptest.main import main
    from ptest.plistener import test_listeners
    test_listeners.set_outer_test_listener(TeamcityTestListener())
    main()


if __name__ == '__main__':
    _main()