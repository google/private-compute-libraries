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

package com.google.android.libraries.pcc.chronicle.tools.ledger;

import static java.util.Objects.requireNonNull;

/**
 * Simple command-line tool that reads all {@link LedgerMap} instances in a directory, merges them,
 * and outputs the merged result to a specified file.
 *
 * <p>To run:
 *
 * <pre>{@code
 * $> TMP_FILES_LOC=/loc/with/individual/ledgermaps \
 *   LEDGER_RESULT_FILE=/output/location.json \
 *   LedgerMerge
 * }</pre>
 */
public final class LedgerMerge {
  public static void main(String[] args) {
    String inputDir = requireNonNull(System.getenv("TMP_FILES_LOC"));
    String outputLocation = requireNonNull(System.getenv("LEDGER_RESULT_FILE"));

    LedgerMap map = DataHubLedger.mergeLedgers(inputDir, true);
    map.writeTo(outputLocation);
  }

  private LedgerMerge() {}
}
