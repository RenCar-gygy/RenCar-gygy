package com.turkcell.rencarapp.data.payment

import com.turkcell.rencarapp.data.auth.AuthorizedRequestExecutor
import com.turkcell.rencarapp.data.network.api.IyzicoApi
import com.turkcell.rencarapp.data.network.dto.CheckoutFormInitializeResponseDto
import com.turkcell.rencarapp.data.network.dto.InitializeCheckoutFormDto
import com.turkcell.rencarapp.data.network.dto.IyzicoPaymentResponseDto
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DefaultIyzicoRepository @Inject constructor(
    private val api: IyzicoApi,
    private val authorizedRequestExecutor: AuthorizedRequestExecutor,
) : IyzicoRepository {

    override suspend fun initializeCheckoutForm(
        request: InitializeCheckoutFormDto,
    ): Result<CheckoutFormInitializeResponseDto> =
        authorizedCall { authorization ->
            api.initializeCheckoutForm(authorization = authorization, request = request)
        }

    override suspend fun getCheckoutFormResult(token: String): Result<IyzicoPaymentResponseDto> =
        authorizedCall { authorization ->
            api.getCheckoutFormResult(authorization = authorization, token = token)
        }

    private suspend fun <T> authorizedCall(block: suspend (authorization: String) -> T): Result<T> =
        authorizedRequestExecutor.execute(block)
}
