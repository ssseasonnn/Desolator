package zlc.season.desolator.util

import java.lang.reflect.Field
import java.lang.reflect.Method
import kotlin.reflect.KClass

fun Class(name: String): Class<*> {
    return Class.forName(name)
}

fun Class<*>.field(name: String): Field {
    val field = getDeclaredField(name)
    field.isAccessible = true
    return field
}

fun Field.of(who: Any?): Any {
    return get(who)!!
}

fun Field.proxy(who: Any, createProxyObj: (Any) -> Any) {
    val origin = get(who)
    val proxyObj = createProxyObj(origin!!)
    set(who, proxyObj)
}

fun Class<*>.method(methodName: String, vararg args: KClass<*>): Method {
    val method = getDeclaredMethod(methodName, *args.param())
    method.isAccessible = true
    return method
}

fun Array<out KClass<*>>.param(): Array<Class<*>> {
    val result = mutableListOf<Class<*>>()
    forEach {
        result.add(it.java)
    }
    return result.toTypedArray()
}