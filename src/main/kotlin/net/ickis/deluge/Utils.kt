package net.ickis.deluge

import kotlinx.coroutines.CoroutineExceptionHandler
import mu.KLogger

fun exceptionHandler(logger: KLogger) = CoroutineExceptionHandler { ctx, ex ->
    logger.error("Context $ctx", ex)
}

/**
 * Checks that a when expression is exhaustive.
 */
val <T> T.exhaustive: T
    get() = this
