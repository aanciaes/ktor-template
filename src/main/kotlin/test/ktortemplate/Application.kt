package test.ktortemplate

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.application.log
import io.ktor.features.CORS
import io.ktor.features.CallLogging
import io.ktor.features.Compression
import io.ktor.features.ContentNegotiation
import io.ktor.features.DefaultHeaders
import io.ktor.features.deflate
import io.ktor.features.gzip
import io.ktor.features.identity
import io.ktor.gson.GsonConverter
import io.ktor.http.ContentType
import io.ktor.http.HttpMethod
import io.ktor.routing.Routing
import io.ktor.util.KtorExperimentalAPI
import org.koin.ktor.ext.Koin
import test.ktortemplate.conf.DevEnvironmentConfigurator
import test.ktortemplate.conf.ProdEnvironmentConfigurator
import test.ktortemplate.core.httphandler.defaultRoutes
import test.ktortemplate.core.utils.JsonSettings

@KtorExperimentalAPI
fun Application.module() {

    val modules = when {
        isDev -> DevEnvironmentConfigurator(environment).buildEnvironmentConfig()
        isProd -> ProdEnvironmentConfigurator(environment).buildEnvironmentConfig()
        else -> DevEnvironmentConfigurator(environment).buildEnvironmentConfig()
    }

    install(DefaultHeaders)
    install(Compression) {
        gzip {
            priority = 100.0
        }
        identity {
            priority = 10.0
        }
        deflate {
            priority = 1.0
        }
    }

    install(CallLogging) {
        level = org.slf4j.event.Level.INFO
    }
    install(ContentNegotiation) {
        register(ContentType.Application.Json, GsonConverter(JsonSettings.mapper))
    }

    install(CORS) {
        method(HttpMethod.Options)
        method(HttpMethod.Get)
        method(HttpMethod.Post)
        method(HttpMethod.Put)
        method(HttpMethod.Delete)
        anyHost()
    }

    install(Koin) {
        modules(modules)
    }

    install(Routing) {
        defaultRoutes()
    }

    log.info("Ktor server started...")
}

val Application.envKind get() = environment.config.property("ktor.environment").getString()
val Application.isDev get() = envKind == "dev"
val Application.isProd get() = envKind == "prod"
