package org.springframework.samples.petclinic.system;

// REMOVED: Spring WebConfiguration class
// The original class configured:
// - SessionLocaleResolver for i18n locale tracking
// - LocaleChangeInterceptor for ?lang= URL parameter
// - WebMvcConfigurer for interceptor registration
//
// In Quarkus, i18n is handled via Qute's message bundles.
// The locale interceptor pattern is not directly supported.
// TODO: Migration required — if locale switching via ?lang= URL parameter is needed,
// implement a custom JAX-RS filter to handle locale changes.
