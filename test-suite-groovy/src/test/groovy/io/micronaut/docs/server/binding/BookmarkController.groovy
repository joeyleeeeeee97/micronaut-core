/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.docs.server.binding

// tag::imports[]
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get

import javax.annotation.Nullable
import jakarta.validation.Valid
// end::imports[]

// tag::class[]
@Controller("/api")
class BookmarkController {

    @Get("/bookmarks/list{?paginationCommand*}")
    HttpStatus list(@Valid @Nullable PaginationCommand paginationCommand) {
        HttpStatus.OK
    }
}
// end::class[]
