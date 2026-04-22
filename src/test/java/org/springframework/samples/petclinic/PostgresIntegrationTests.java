/*
 * Copyright 2012-2025 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.samples.petclinic;

// TODO: Migration required — PostgreSQL integration tests need Quarkus DevServices or
// QuarkusTestResource with Testcontainers. Spring Boot docker-compose integration
// and @ServiceConnection are not supported in Quarkus.
// To re-enable, use @QuarkusTest with quarkus.datasource.devservices configuration
// or implement a QuarkusTestResourceLifecycleManager for PostgreSQL.
