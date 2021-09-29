package com.passbolt.mobile.android.core.networking

import coil.util.CoilUtils
import com.passbolt.mobile.android.common.MfaTokenExtractor
import com.passbolt.mobile.android.core.networking.interceptor.AuthInterceptor
import com.passbolt.mobile.android.core.networking.interceptor.ChangeableBaseUrlInterceptor
import com.passbolt.mobile.android.core.networking.interceptor.CookiesInterceptor
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import org.koin.android.ext.koin.androidContext
import org.koin.core.qualifier.named
import org.koin.dsl.module
import java.time.Duration

/**
 * Passbolt - Open source password manager for teams
 * Copyright (c) 2021 Passbolt SA
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General
 * Public License (AGPL) as published by the Free Software Foundation version 3.
 *
 * The name "Passbolt" is a registered trademark of Passbolt SA, and Passbolt SA hereby declines to grant a trademark
 * license to "Passbolt" pursuant to the GNU Affero General Public License version 3 Section 7(e), without a separate
 * agreement with Passbolt SA.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program. If not,
 * see GNU Affero General Public License v3 (http://www.gnu.org/licenses/agpl-3.0.html).
 *
 * @copyright Copyright (c) Passbolt SA (https://www.passbolt.com)
 * @license https://opensource.org/licenses/AGPL-3.0 AGPL License
 * @link https://www.passbolt.com Passbolt (tm)
 * @since v1.0
 */
val networkingModule = module {
    single { provideLoggingInterceptor() }
    single(named(DEFAULT_HTTP_CLIENT)) {
        provideHttpClient(
            loggingInterceptor = get(),
            interceptors = listOf(
                get<ChangeableBaseUrlInterceptor>(),
                get<AuthInterceptor>(),
                get<CookiesInterceptor.ReceivedCookiesInterceptor>(),
                get<CookiesInterceptor.AddCookiesInterceptor>()
            )
        )
    }
    single(named(COIL_HTTP_CLIENT)) {
        provideCoilHttpClient(
            loggingInterceptor = get(),
            changeableBaseUrlInterceptor = get(),
            cache = get()
        )
    }
    single { ChangeableBaseUrlInterceptor(getCurrentApiUrlUseCase = get()) }
    single {
        AuthInterceptor(
            getSessionUseCase = get()
        )
    }
    single {
        CookiesInterceptor.ReceivedCookiesInterceptor(
            mfaTokenExtractor = get()
        )
    }
    single {
        CookiesInterceptor.AddCookiesInterceptor(
            getSessionUseCase = get()
        )
    }
    single { MfaTokenExtractor() }
    single {
        RetrofitRestService(
            client = get(),
            converterFactory = get()
        )
    }
    single {
        ResponseHandler(
            errorHeaderMapper = get()
        )
    }
    single {
        ErrorHeaderMapper(
            gson = get(),
            context = get()
        )
    }
    single { CoilUtils.createDefaultCache(androidContext()) }
}

private fun provideLoggingInterceptor(): HttpLoggingInterceptor {
    return HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }
}

private fun provideHttpClient(
    loggingInterceptor: HttpLoggingInterceptor,
    interceptors: List<Interceptor>
) =
    OkHttpClient.Builder()
        .hostnameVerifier { _, _ -> true } // TODO remove in production version - PAS-105
        .addNetworkInterceptor(loggingInterceptor)
        .connectTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
        .writeTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
        .readTimeout(Duration.ofSeconds(TIMEOUT_SECONDS))
        .apply {
            interceptors.forEach { addInterceptor(it) }
        }
        .build()

private fun provideCoilHttpClient(
    loggingInterceptor: HttpLoggingInterceptor,
    changeableBaseUrlInterceptor: ChangeableBaseUrlInterceptor,
    cache: Cache
) =
    OkHttpClient.Builder()
        .addNetworkInterceptor(loggingInterceptor)
        .addInterceptor(changeableBaseUrlInterceptor)
        .hostnameVerifier { _, _ -> true } // TODO remove in production version - PAS-105
        .cache(cache)
        .build()

const val DEFAULT_HTTP_CLIENT = "DEFAULT_HTTP_CLIENT"
const val COIL_HTTP_CLIENT = "COIL_HTTP_CLIENT"
