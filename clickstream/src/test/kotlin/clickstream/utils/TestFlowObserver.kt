package clickstream.utils

import io.reactivex.processors.PublishProcessor
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.reactive.asPublisher
import org.assertj.core.api.Assertions.assertThat

public class TestFlowObserver<out T : Any>(stream: Flow<T>) {
    private val publishProcessor = PublishProcessor.create<T>()
    private val testSubscriber = publishProcessor.test()

    init {
        stream.asPublisher().subscribe(publishProcessor)
    }

    public val values: List<T>
        get() = testSubscriber.values()

    public val errors: List<Throwable>
        get() = testSubscriber.errors()

    public val completions: Long
        get() = testSubscriber.completions()

    public fun awaitCount(exactly: Int) {
        testSubscriber.awaitCount(exactly)
        assertThat(values.size).isEqualTo(exactly)
    }

    public fun awaitValues(vararg valueAsserts: ValueAssert<Any>) {
        awaitCount(valueAsserts.size)
        testSubscriber.assertNoErrors()
        valueAsserts.zip(values).forEachIndexed { index, (valueAssert, value) ->
            try {
                valueAssert.execute(value)
            } catch (cause: AssertionError) {
                throw AssertionError("Assertion at index $index failed", cause)
            }
        }
    }
}
