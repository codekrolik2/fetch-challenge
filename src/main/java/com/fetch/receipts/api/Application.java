package com.fetch.receipts.api;

import ru.tinkoff.kora.application.graph.KoraApplication;
import ru.tinkoff.kora.common.KoraApp;
import ru.tinkoff.kora.config.hocon.HoconConfigModule;
import ru.tinkoff.kora.http.server.common.HttpServerResponseException;
import ru.tinkoff.kora.http.server.undertow.UndertowHttpServerModule;
import ru.tinkoff.kora.json.module.JsonModule;
import ru.tinkoff.kora.logging.logback.LogbackModule;
import ru.tinkoff.kora.validation.module.ValidationModule;
import ru.tinkoff.kora.validation.module.http.server.ViolationExceptionHttpServerResponseMapper;

@KoraApp
public interface Application extends
        HoconConfigModule,
        LogbackModule,
        ValidationModule,
        JsonModule,
        UndertowHttpServerModule {

    default ViolationExceptionHttpServerResponseMapper customViolationExceptionHttpServerResponseMapper() {
        return (request, exception) -> HttpServerResponseException.of(400, exception.getMessage());
    }

    static void main(String[] args) {
        KoraApplication.run(ApplicationGraph::graph);
    }
}
