/*
 * Copyright 2022 Google LLC
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

package com.google.android.libraries.pcc.chronicle.annotation

import com.google.android.libraries.pcc.chronicle.api.StorageMedia

/**
 * This annotation can be used to describe a [ManagementStrategy] for Chronicle data. When the
 * [DataCacheStoreAnnotationProcessor] plugin is present, it will operate on all types with this
 * annotation. When processed, this annotation will generate a [ManagementStrategy] object for the
 * data. The parameter `storageMedia` can be one of the [StorageMedia] enumerated types, or the
 * default is `StorageMedia.MEMORY`.
 */
@Retention(AnnotationRetention.SOURCE)
@Target(AnnotationTarget.CLASS)
annotation class DataCacheStore(
  val ttl: String,
  val maxItems: Int,
  val storageMedia: StorageMedia = StorageMedia.MEMORY,
)
