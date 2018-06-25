package io.openfuture.api.component.web3.event.decoder

import io.openfuture.api.component.web3.event.domain.ActivatedScaffoldEvent
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Bool
import org.web3j.abi.datatypes.generated.Uint8

class ActivatedScaffoldDecoder : Decoder<ActivatedScaffoldEvent> {

    override fun decode(addressScaffold: String, rawData: String): ActivatedScaffoldEvent {
        val response = getResponse(rawData, listOf(object : TypeReference<Uint8>() {}, object : TypeReference<Bool>() {}))

        val activated = response[1].value as Boolean

        return ActivatedScaffoldEvent(activated)
    }

}