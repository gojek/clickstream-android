package clickstream.utils

public class ValueAssert<out T : Any> {
    private val assertions = mutableListOf<(Any) -> Unit>()

    public fun assert(assertion: T.() -> Unit): ValueAssert<T> = apply {
        assertions.add {
            @Suppress("UNCHECKED_CAST")
            (it as T).assertion()
        }
    }

    public fun execute(value: Any): Unit = assertions.forEach { it(value) }
}
