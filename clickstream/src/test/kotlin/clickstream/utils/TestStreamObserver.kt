package clickstream.utils

import com.tinder.scarlet.Stream
import io.reactivex.processors.PublishProcessor
import org.assertj.core.api.Assertions.assertThat

public class TestStreamObserver<out T : Any>(stream: Stream<T>) {
    private val publishProcessor = PublishProcessor.create<T>()
    private val testSubscriber = publishProcessor.test()

    init {
        stream.subscribe(publishProcessor)
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
