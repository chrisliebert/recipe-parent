/* Copyright 2022 Chris Liebert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
module com.clieb.kitchen.frontend {
    requires transitive javafx.graphics;
    requires transitive java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires org.reactivestreams;
    requires io.micronaut.core;
    requires io.micronaut.http;
    requires io.micronaut.http_client;
    requires io.micronaut.http_client_core;
    requires io.micronaut.inject;
    requires io.micronaut.context;
    requires io.micronaut.validation;
    requires io.netty.handler;
    requires io.micronaut.data.data_model;
    requires io.micronaut.data.data_r2dbc;
    requires io.micronaut.core_reactive;
    requires io.micronaut.reactor.reactor;
    requires io.micronaut.jackson_core;
    requires io.micronaut.jackson_databind;
    requires io.micronaut.aop;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.validation;
    requires reactor.core;
    requires jakarta.annotation;
    requires jakarta.inject;
    requires java.annotation;
    requires com.clieb.kitchen.database.model;
    opens com.clieb.kitchen.frontend.model to com.fasterxml.jackson.databind;
    opens com.clieb.kitchen.frontend to
            javafx.fxml,
            javafx.graphics,
            org.reactivestreams,
            io.micronaut.inject,
            io.micronaut.core,
            io.micronaut.http,
            io.micronaut.http_client,
            io.micronaut.http_client_core,
            io.micronaut.context,
            io.micronaut.validation,
            io.netty.handler,
            io.micronaut.data.data_model,
            io.micronaut.data.data_r2dbc,
            io.micronaut.core_reactive,
            io.micronaut.reactor.reactor,
            io.micronaut.jackson_core,
            io.micronaut.jackson_databind,
            io.micronaut.aop,
            com.fasterxml.jackson.annotation,
            com.fasterxml.jackson.core,
            com.fasterxml.jackson.databind,
            java.validation,
            reactor.core,
            jakarta.annotation,
            jakarta.inject,
            java.annotation,
            com.clieb.kitchen.database.model,
            com.clieb.kitchen.frontend;
    opens com.clieb.kitchen.frontend.controller  to
            javafx.fxml,
            javafx.graphics,
            org.reactivestreams,
            io.micronaut.inject,
            io.micronaut.core,
            io.micronaut.http,
            io.micronaut.http_client,
            io.micronaut.http_client_core,
            io.micronaut.context,
            io.micronaut.validation,
            io.netty.handler,
            io.micronaut.data.data_model,
            io.micronaut.data.data_r2dbc,
            io.micronaut.core_reactive,
            io.micronaut.reactor.reactor,
            io.micronaut.jackson_core,
            io.micronaut.jackson_databind,
            io.micronaut.aop,
            com.fasterxml.jackson.annotation,
            com.fasterxml.jackson.core,
            com.fasterxml.jackson.databind,
            java.validation,
            reactor.core,
            jakarta.annotation,
            jakarta.inject,
            java.annotation,
            com.clieb.kitchen.database.model,
            com.clieb.kitchen.frontend;
    opens com.clieb.kitchen.frontend.subscriber  to
            javafx.fxml,
            javafx.graphics,
            org.reactivestreams,
            io.micronaut.inject,
            io.micronaut.core,
            io.micronaut.http,
            io.micronaut.http_client,
            io.micronaut.http_client_core,
            io.micronaut.context,
            io.micronaut.validation,
            io.netty.handler,
            io.micronaut.data.data_model,
            io.micronaut.data.data_r2dbc,
            io.micronaut.core_reactive,
            io.micronaut.reactor.reactor,
            io.micronaut.jackson_core,
            io.micronaut.jackson_databind,
            io.micronaut.aop,
            com.fasterxml.jackson.annotation,
            com.fasterxml.jackson.core,
            com.fasterxml.jackson.databind,
            java.validation,
            reactor.core,
            jakarta.annotation,
            jakarta.inject,
            java.annotation,
            com.clieb.kitchen.database.model,
            com.clieb.kitchen.frontend;
    exports com.clieb.kitchen.frontend;
    exports com.clieb.kitchen.frontend.model;
    exports com.clieb.kitchen.frontend.controller;
    exports com.clieb.kitchen.frontend.subscriber;
}
