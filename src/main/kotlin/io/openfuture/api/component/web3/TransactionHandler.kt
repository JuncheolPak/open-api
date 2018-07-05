package io.openfuture.api.component.web3

import io.openfuture.api.component.web3.event.ProcessorEventDecoder
import io.openfuture.api.domain.transaction.TransactionDto
import io.openfuture.api.entity.scaffold.Transaction
import io.openfuture.api.repository.ScaffoldRepository
import io.openfuture.api.service.TransactionService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.client.RestTemplate
import org.web3j.protocol.core.methods.response.Log

@Component
class TransactionHandler(
        private val service: TransactionService,
        private val repository: ScaffoldRepository,
        private val eventDecoder: ProcessorEventDecoder
) {

    companion object {
        private val log = LoggerFactory.getLogger(TransactionHandler::class.java)
    }


    @Transactional
    fun handle(transactionLog: Log) {
        val contract = repository.findByAddress(transactionLog.address)

        if (null == contract) {
            log.warn("Scaffold with address ${transactionLog.address} not found")
            return
        }

        try {
            val transaction = service.save(Transaction.of(contract, transactionLog))
            val event = eventDecoder.getEvent(contract.address, transaction.data)
            contract.webHook?.let { RestTemplate().postForLocation(it, TransactionDto(transaction, event)) }
        } catch (e: Exception) {
            log.warn(e.message)
        }
    }

}