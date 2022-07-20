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
module com.clieb.kitchen.server.test {
    requires com.clieb.kitchen.server;
    requires io.micronaut.validation;
    requires io.micronaut.core;
    requires io.micronaut.data.data_model;
    requires io.micronaut.data.data_r2dbc;
    requires io.micronaut.inject;
    requires io.micronaut.core_reactive;
    requires io.micronaut.reactor.reactor;
    requires io.micronaut.jackson_core;
    requires io.micronaut.jackson_databind;
    requires io.micronaut.aop;
    requires io.micronaut.http;
    requires io.micronaut.context;
    requires io.micronaut.router;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires java.validation;
    requires org.reactivestreams;
    requires reactor.core;
    requires jakarta.annotation;
    requires jakarta.inject;
    requires org.jsoup;
    requires java.annotation;
    requires org.junit.jupiter.api;
    requires org.junit.jupiter.engine;
    requires org.junit.platform.commons;
    requires org.junit.platform.engine;
    requires com.clieb.kitchen.database.model;
    opens com.clieb.kitchen.server.test to java.sql,
        com.fasterxml.jackson.annotation,
        com.fasterxml.jackson.core,
        com.fasterxml.jackson.databind,
        java.validation,
        org.reactivestreams,
        reactor.core,
        jakarta.annotation,
        jakarta.inject,
        org.jsoup,
        java.annotation,
        io.micronaut.validation,
        io.micronaut.core,
        io.micronaut.data.data_model,
        io.micronaut.data.data_r2dbc,
        io.micronaut.inject,
        io.micronaut.core_reactive,
        io.micronaut.reactor.reactor,
        io.micronaut.jackson_core,
        io.micronaut.jackson_databind,
        io.micronaut.aop,
        io.micronaut.http,
        io.micronaut.context,
        io.micronaut.router,
        org.junit.jupiter.api,
        org.junit.jupiter.engine,
        org.junit.platform.commons,
        org.junit.platform.engine,
        com.clieb.kitchen.server;
}