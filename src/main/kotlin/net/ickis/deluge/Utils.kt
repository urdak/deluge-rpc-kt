package net.ickis.deluge

import org.apache.logging.log4j.Logger
import kotlinx.coroutines.CoroutineExceptionHandler

fun exceptionHandler(logger: Logger) = CoroutineExceptionHandler { ctx, ex ->
    logger.error("Context $ctx", ex)
}
