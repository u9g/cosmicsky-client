package dev.u9g.util

fun <T : Any> T.iterate(iterator: (T) -> T?): Sequence<T> = sequence {
    var x: T? = this@iterate
    while (x != null) {
        yield(x)
        x = iterator(x)
    }
}
