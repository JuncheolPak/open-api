package io.openfuture.api.exception

import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ResponseStatus

/**
 * @author Kadach Alexey
 */
@ResponseStatus(code = HttpStatus.NOT_FOUND, reason = "Not Found")
open class NotFoundException(message: String?) : RuntimeException(message)