package io.openfuture.api.controller.api

import io.openfuture.api.component.web3.Web3Wrapper
import io.openfuture.api.config.ControllerTests
import io.openfuture.api.config.propety.EthereumProperties
import io.openfuture.api.entity.auth.Role
import org.junit.Test
import org.mockito.BDDMockito.given
import org.mockito.Mock
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpHeaders.AUTHORIZATION
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.web3j.crypto.Credentials
import org.web3j.spring.autoconfigure.Web3jProperties

@WebMvcTest(FrontendPropertiesApiController::class)
class FrontendPropertiesApiControllerTests : ControllerTests() {

    @MockBean
    private lateinit var web3: Web3Wrapper

    @MockBean
    private lateinit var web3Properties: Web3jProperties

    @MockBean
    private lateinit var properties: EthereumProperties

    @Mock
    private lateinit var credentials: Credentials


    @Test
    fun getTest() {
        val openKey = createOpenKey(setOf(Role("ROLE_MASTER")))
        val version = "version"
        val clientAddress = "clientAddress"
        val openTokenAddress = "address"
        val platformAddress = "address"

        given(keyService.find(openKey.value)).willReturn(openKey)
        given(web3.getNetVersion()).willReturn(version)
        given(web3Properties.clientAddress).willReturn(clientAddress)
        given(properties.openTokenAddress).willReturn(openTokenAddress)
        given(properties.getCredentials()).willReturn(credentials)
        given(credentials.address).willReturn(platformAddress)

        mvc.perform(get("/api/properties")
                .header(AUTHORIZATION, openKey.value))

                .andExpect(status().isOk)
                .andExpect(content().json("""
                    {
                        "networkAddress": $clientAddress,
                        "networkVersion": $version,
                        "openTokenAddress": $openTokenAddress,
                        "platformAddress": $platformAddress
                    }
                    """.trimIndent(), true))
    }

    @Test
    fun getWhenOpenTokenIsNotFoundShouldRedirectToIndexPageTest() {
        val invalidToken = "not_valid_token"

        given(keyService.find(invalidToken)).willReturn(null)

        mvc.perform(get("/api/properties")
                .header(AUTHORIZATION, invalidToken))

                .andExpect(status().isUnauthorized)
    }

}
