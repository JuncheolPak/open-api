package io.openfuture.api.component.web3.event.decoder

import io.openfuture.api.component.web3.event.domain.EditedShareHolderEvent
import org.web3j.abi.TypeReference
import org.web3j.abi.datatypes.Address
import org.web3j.abi.datatypes.generated.Uint256
import org.web3j.abi.datatypes.generated.Uint8
import java.math.BigInteger

class EditedShareHolderDecoder : Decoder<EditedShareHolderEvent> {

    override fun decode(addressScaffold: String, rawData: String): EditedShareHolderEvent {
        val response = getResponse(rawData, listOf(object : TypeReference<Uint8>() {}, object : TypeReference<Address>() {},
                object : TypeReference<Uint256>() {}))

        val userAddress = response[1].value as String
        val partnerShare = response[2].value as BigInteger

        return EditedShareHolderEvent(userAddress, partnerShare)
    }

}