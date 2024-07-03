package dev.u9g.util.render

val pi = Math.PI
val tau = Math.PI * 2
fun lerpAngle(a: Float, b: Float, progress: Float): Float {
    val shortestAngle = ((((b.mod(tau) - a.mod(tau)).mod(tau)) + tau + pi).mod(tau)) - pi
    return ((a + (shortestAngle) * progress).mod(tau)).toFloat()
}

fun lerp(a: Float, b: Float, progress: Float): Float {
    return a + (b - a) * progress
}

fun ilerp(a: Float, b: Float, value: Float): Float {
    return (value - a) / (b - a)
}