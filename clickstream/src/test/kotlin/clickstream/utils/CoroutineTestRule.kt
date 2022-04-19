package clickstream.utils

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.TestCoroutineScope
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.rules.TestWatcher
import org.junit.runner.Description

@ExperimentalCoroutinesApi
public class CoroutineTestRule constructor(
    public val testDispatcher: TestCoroutineDispatcher = TestCoroutineDispatcher(),
    // TODO the job should be remove once kotlin fix the issue
    // re: https://github.com/Kotlin/kotlinx.coroutines/issues/2379
    public val scope: TestCoroutineScope = TestCoroutineScope(Job() + testDispatcher)
) : TestWatcher() {

    override fun starting(description: Description?) {
        super.starting(description)
        Dispatchers.setMain(testDispatcher)
    }

    override fun finished(description: Description?) {
        super.finished(description)
        Dispatchers.resetMain()
        scope.cancel()
        // this is essentially to propagate any exception after tests are run
        scope.cleanupTestCoroutines()
    }
}
