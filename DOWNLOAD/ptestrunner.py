try:
    import ptest
except ImportError:
    raise NameError("No ptest runner found in selected interpreter")

try:
    from tcmessages import TeamcityServiceMessages
except ImportError:
    raise NameError("No tcmessages module found in selected interpreter")


class TeamcityTestListener(object):
    def __init__(self):
        self.messages = TeamcityServiceMessages(prepend_linebreak=True)

    def on_test_suite_start(self, test_suite):
        self.messages.testMatrixEntered()
        self.messages.testCount(len(test_suite.test_case_names))
        self.messages.testSuiteStarted(suiteName="Default Suite")

    def on_test_suite_finish(self, test_suite):
        self.messages.testSuiteFinished(suiteName="Default Suite")

    def on_test_case_start(self, test_case):
        self.messages.testStarted(testName=test_case.full_name)

    def on_test_case_finish(self, test_case):
        from ptest.enumeration import TestCaseStatus
        if test_case.status == TestCaseStatus.FAILED:
            self.messages.testFailed(testName=test_case.full_name)
        elif test_case.status == TestCaseStatus.SKIPPED:
            self.messages.testIgnored(testName=test_case.full_name)
        self.messages.testFinished(testName=test_case.full_name, duration=int(test_case.elapsed_time * 1000.0))


def _main():
    from ptest.main import main
    from ptest.plistener import test_listeners
    test_listeners.append(TeamcityTestListener())
    main()


if __name__ == '__main__':
    _main()
