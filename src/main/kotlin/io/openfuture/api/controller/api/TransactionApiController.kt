package io.openfuture.api.controller.api

import io.openfuture.api.annotation.CurrentUser
import io.openfuture.api.component.event.AddTransactionEvent
import io.openfuture.api.component.web3.event.ProcessorEventDecoder
import io.openfuture.api.domain.PageRequest
import io.openfuture.api.domain.PageResponse
import io.openfuture.api.domain.transaction.TransactionDto
import io.openfuture.api.entity.auth.User
import io.openfuture.api.service.ScaffoldService
import io.openfuture.api.service.TransactionService
import org.springframework.context.ApplicationListener
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.async.DeferredResult
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

@RestController
@RequestMapping("/api/scaffolds/{address}/transactions")
class TransactionApiController(
        private val service: TransactionService,
        private val scaffoldService: ScaffoldService,
        private val eventDecoder: ProcessorEventDecoder
) : ApplicationListener<AddTransactionEvent> {

    private val promises = ConcurrentHashMap<String, CopyOnWriteArrayList<DeferredResult<PageResponse<TransactionDto>>>>()

    companion object {
        private const val RESULT_TIMEOUT = 30000L
    }


    @GetMapping
    fun getAll(@CurrentUser user: User, @PathVariable address: String,
               pageRequest: PageRequest): PageResponse<TransactionDto> {
        val scaffold = scaffoldService.get(address, user)
        val transactions = service.getAll(scaffold, pageRequest)
                .map { TransactionDto(it, eventDecoder.getEvent(it.scaffold.address, it.data)) }
        return PageResponse(transactions)
    }

    @GetMapping("/updates")
    fun getUpdates(@PathVariable address: String): DeferredResult<PageResponse<TransactionDto>> {
        val promise = DeferredResult<PageResponse<TransactionDto>>(RESULT_TIMEOUT, PageResponse<TransactionDto>(listOf()))
        promise.onCompletion { promises[address]?.remove(promise) }

        if (null == promises[address]) {
            promises[address] = CopyOnWriteArrayList()
        }

        promises[address]!!.add(promise)

        return promise
    }

    override fun onApplicationEvent(event: AddTransactionEvent) {
        val transaction = event.transaction
        val scaffoldAddress = transaction.scaffold.address

        promises[scaffoldAddress]?.forEach { it.setResult(PageResponse(listOf(transaction))) }
        promises.remove(scaffoldAddress)
    }

}